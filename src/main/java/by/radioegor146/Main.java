package by.radioegor146;

import com.sun.javafx.css.Combinator;
import com.sun.javafx.css.CompoundSelector;
import com.sun.javafx.css.Selector;
import com.sun.javafx.css.SimpleSelector;
import com.sun.javafx.css.Size;
import com.sun.javafx.css.Stylesheet;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.EffectConverter;
import com.sun.javafx.css.converters.FontConverter;
import com.sun.javafx.css.converters.InsetsConverter;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.css.converters.SizeConverter;
import com.sun.javafx.css.converters.StringConverter;
import com.sun.javafx.css.converters.URLConverter;
import com.sun.javafx.scene.layout.region.BorderStyleConverter;
import com.sun.javafx.scene.layout.region.CornerRadiiConverter;
import com.sun.javafx.scene.layout.region.LayeredBackgroundPositionConverter;
import com.sun.javafx.scene.layout.region.LayeredBackgroundSizeConverter;
import com.sun.javafx.scene.layout.region.LayeredBorderPaintConverter;
import com.sun.javafx.scene.layout.region.LayeredBorderStyleConverter;
import com.sun.javafx.scene.layout.region.Margins;
import com.sun.javafx.scene.layout.region.RepeatStructConverter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.FontWeight;

public class Main {

    private static final DecimalFormat DF4 = new DecimalFormat("#.####");

    @FunctionalInterface
    private static interface SingleConverter {

        void process(StringBuilder cssStringBuilder, ParsedValue parsedValue, StyleConverter converter, Object value);
    }

    private static final Map<Class, SingleConverter> CONVERTERS = new HashMap<>();
    private static Map<Color, String> PREDEFINED_COLORS = null;

    private static void registerConverters() {
        CONVERTERS.put(null, (cssStringBuilder, parsedValue, converter, value) -> {
            if (value instanceof String) {
                appendValue(cssStringBuilder, String.format("\"%s\"", ((String) value).replace("\\", "\\\\").replace("\"", "\\\"")));
            } else {
                appendValue(cssStringBuilder, value);
            }
        });
        CONVERTERS.put(SizeConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            appendValue(cssStringBuilder, ((ParsedValue) value).getValue());
        });
        CONVERTERS.put(StringConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            appendValue(cssStringBuilder, value);
        });
        CONVERTERS.put(FontConverter.FontSizeConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            appendValue(cssStringBuilder, ((ParsedValue) value).getValue());
        });
        CONVERTERS.put(BooleanConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            appendValue(cssStringBuilder, value);
        });
        CONVERTERS.put(InsetsConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] sides = (ParsedValue[]) value;
            if (sides.length == 0) {
                cssStringBuilder.append("0");
            }
            for (int i = 0; i < sides.length; i++) {
                appendValue(cssStringBuilder, sides[i].getValue());
                if (i != sides.length - 1) {
                    cssStringBuilder.append(" ");
                }
            }
        });
        CONVERTERS.put(InsetsConverter.SequenceConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] layeredSides = (ParsedValue[]) value;
            for (int i = 0; i < layeredSides.length; i++) {
                ParsedValue[] sides = (ParsedValue[]) layeredSides[i].getValue();
                if (sides.length == 0) {
                    cssStringBuilder.append("0");
                }
                for (int j = 0; j < sides.length; j++) {
                    appendValue(cssStringBuilder, sides[j].getValue());
                    if (j != sides.length - 1) {
                        cssStringBuilder.append(" ");
                    }
                }
                if (i != layeredSides.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
        CONVERTERS.put(FontConverter.FontWeightConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            FontWeight weight = null;

            if (value instanceof String) {
                try {
                    String sval = ((String) value).toUpperCase(Locale.ROOT);
                    weight = Enum.valueOf(FontWeight.class, sval);
                } catch (IllegalArgumentException | NullPointerException iae) {
                    weight = FontWeight.NORMAL;
                }
            } else if (value instanceof FontWeight) {
                weight = (FontWeight) value;
            }

            String cssWeightName = "null";

            if (weight != null) {
                switch (weight) {
                    case THIN:
                        cssWeightName = "thin";
                        break;
                    case EXTRA_LIGHT:
                        cssWeightName = "extra-light";
                        break;
                    case LIGHT:
                        cssWeightName = "light";
                        break;
                    case NORMAL:
                        cssWeightName = "normal";
                        break;
                    case MEDIUM:
                        cssWeightName = "medium";
                        break;
                    case SEMI_BOLD:
                        cssWeightName = "semi-bold";
                        break;
                    case BOLD:
                        cssWeightName = "bold";
                        break;
                    case EXTRA_BOLD:
                        cssWeightName = "extra-bold";
                        break;
                    case BLACK:
                        cssWeightName = "black";
                        break;
                }
            }

            cssStringBuilder.append(cssWeightName);
        });
        CONVERTERS.put(URLConverter.SequenceConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] values = (ParsedValue[]) value;
            if (values.length == 0) {
                cssStringBuilder.append("null");
            }
            for (int i = 0; i < values.length; i++) {
                ParsedValue[] subValues = (ParsedValue[]) values[i].getValue();
                cssStringBuilder.append("url(").append(subValues[0].getValue()).append(")");
                if (i != values.length - 1) {
                    cssStringBuilder.append(" ");
                }
            }
        });
        CONVERTERS.put(PaintConverter.SequenceConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] values = (ParsedValue[]) value;
            if (values.length == 0) {
                cssStringBuilder.append("null");
            }
            for (int i = 0; i < values.length; i++) {
                if (values[i].getConverter() == null) {
                    appendValue(cssStringBuilder, values[i].getValue());
                } else {
                    throw new RuntimeException(String.format("Converter '%s' not found", values[i].getConverter().getClass()));
                }
                if (i != values.length - 1) {
                    cssStringBuilder.append(" ");
                }
            }
        });
        CONVERTERS.put(CornerRadiiConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] radiiLayers = (ParsedValue[]) value;
            if (radiiLayers.length != 1) {
                throw new RuntimeException("N/A (CornerRadiiConverter)");
            }
            ParsedValue[][] radiiSet = (ParsedValue[][]) radiiLayers[0].getValue();
            appendValue(cssStringBuilder, radiiSet[0][0].getValue());
            cssStringBuilder.append(" ");
            appendValue(cssStringBuilder, radiiSet[0][1].getValue());
            cssStringBuilder.append(" ");
            appendValue(cssStringBuilder, radiiSet[0][2].getValue());
            cssStringBuilder.append(" ");
            appendValue(cssStringBuilder, radiiSet[0][3].getValue());
            cssStringBuilder.append(", ");
            appendValue(cssStringBuilder, radiiSet[1][0].getValue());
            cssStringBuilder.append(" ");
            appendValue(cssStringBuilder, radiiSet[1][1].getValue());
            cssStringBuilder.append(" ");
            appendValue(cssStringBuilder, radiiSet[1][2].getValue());
            cssStringBuilder.append(" ");
            appendValue(cssStringBuilder, radiiSet[1][3].getValue());
        });
        CONVERTERS.put(Margins.SequenceConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] layeredSides = (ParsedValue[]) value;
            for (int i = 0; i < layeredSides.length; i++) {
                ParsedValue[] sides = (ParsedValue[]) layeredSides[i].getValue();
                if (sides.length == 0) {
                    cssStringBuilder.append("0");
                }
                for (int j = 0; j < sides.length; j++) {
                    appendValue(cssStringBuilder, sides[j].getValue());
                    if (j != sides.length - 1) {
                        cssStringBuilder.append(" ");
                    }
                }
                if (i != layeredSides.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
        CONVERTERS.put(RepeatStructConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[][] layers = (ParsedValue[][]) value;
            for (int i = 0; i < layers.length; i++) {
                ParsedValue[] repeats = layers[i];
                appendValue(cssStringBuilder, repeats[0].convert(null));
                cssStringBuilder.append(" ");
                appendValue(cssStringBuilder, repeats[1].convert(null));
                if (i != layers.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
        CONVERTERS.put(LayeredBorderPaintConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] layeredPaints = (ParsedValue[]) value;
            if (layeredPaints.length == 0) {
                cssStringBuilder.append("null");
            }
            for (int i = 0; i < layeredPaints.length; i++) {
                ParsedValue[] sides = (ParsedValue[]) layeredPaints[i].getValue();
                if (sides.length == 0) {
                    cssStringBuilder.append("black");
                }
                for (int j = 0; j < sides.length; j++) {
                    if (sides[i].getConverter() == null) {
                        appendValue(cssStringBuilder, sides[i].getValue());
                    } else {
                        throw new RuntimeException(String.format("Converter '%s' not found", sides[i].getConverter().getClass()));
                    }
                    if (j != sides.length - 1) {
                        cssStringBuilder.append(" ");
                    }
                }
                if (i != layeredPaints.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
        CONVERTERS.put(LayeredBackgroundPositionConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] layeredPositions = (ParsedValue[]) value;
            if (layeredPositions.length == 0) {
                cssStringBuilder.append("null");
            }
            for (int i = 0; i < layeredPositions.length; i++) {
                ParsedValue[] sides = (ParsedValue[]) layeredPositions[i].getValue();
                for (int j = 0; j < sides.length; j++) {
                    appendValue(cssStringBuilder, sides[i].getValue());
                    if (j != sides.length - 1) {
                        cssStringBuilder.append(" ");
                    }
                }
                if (i != layeredPositions.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
        CONVERTERS.put(EffectConverter.DropShadowConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] shadowProperties = (ParsedValue[]) value;
            for (int i = 0; i < shadowProperties.length; i++) {
                appendValue(cssStringBuilder, shadowProperties[i].convert(null));
                if (i != shadowProperties.length - 1) {
                    cssStringBuilder.append(" ");
                }
            }
        });
        CONVERTERS.put(EffectConverter.InnerShadowConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] shadowProperties = (ParsedValue[]) value;
            for (int i = 0; i < shadowProperties.length; i++) {
                appendValue(cssStringBuilder, shadowProperties[i].convert(null));
                if (i != shadowProperties.length - 1) {
                    cssStringBuilder.append(" ");
                }
            }
        });
        CONVERTERS.put(LayeredBorderStyleConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] layers = (ParsedValue[]) value;
            if (layers.length != 1) {
                throw new RuntimeException("N/A (LayeredBorderStyleConverter)");
            }
            ParsedValue[] sides = (ParsedValue[]) layers[0].getValue();
            for (int i = 0; i < sides.length; i++) {
                final ParsedValue[] values = (ParsedValue[]) sides[i].getValue();

                Object style = values[0];

                final boolean onlyNamed = values[1] == null
                        && values[2] == null
                        && values[3] == null
                        && values[4] == null
                        && values[5] == null;

                String styleName = null;

                if (BorderStyleConverter.NONE.equals(style)) {
                    styleName = "none";
                } else if (BorderStyleConverter.DOTTED.equals(style) && onlyNamed) {
                    styleName = "dotted";
                } else if (BorderStyleConverter.DASHED.equals(style) && onlyNamed) {
                    styleName = "dashed";
                } else if (BorderStyleConverter.SOLID.equals(style) && onlyNamed) {
                    styleName = "solid";
                }

                if (styleName != null) {
                    cssStringBuilder.append(styleName);
                    if (i != sides.length - 1) {
                        cssStringBuilder.append(", ");
                    }
                    continue;
                }

                ParsedValue<?, Size>[] serializedDashes
                        = ((ParsedValue<ParsedValue<?, Size>[], Number[]>) values[0]).getValue();

                List<Size> dashes = null;
                if (serializedDashes == null) {
                    if (BorderStyleConverter.DOTTED.equals(style)) {
                        styleName = "dotted";
                    } else if (BorderStyleConverter.DASHED.equals(style)) {
                        styleName = "dashed";
                    } else if (BorderStyleConverter.SOLID.equals(style)) {
                        styleName = "solid";
                    } else {
                        dashes = Collections.emptyList();
                    }
                } else {
                    dashes = new ArrayList<>(serializedDashes.length);
                    for (ParsedValue<?, Size> dash : serializedDashes) {
                        dashes.add(dash.convert(null));
                    }
                }

                final double phase
                        = (values[1] != null) ? (Double) values[1].convert(null) : 0;

                final StrokeType strokeType
                        = (values[2] != null) ? (StrokeType) values[2].convert(null) : StrokeType.INSIDE;

                final StrokeLineJoin lineJoin
                        = (values[3] != null) ? (StrokeLineJoin) values[3].convert(null) : StrokeLineJoin.MITER;

                final double miterLimit
                        = (values[4] != null) ? (Double) values[4].convert(null) : 10;

                final StrokeLineCap lineCap
                        = (values[5] != null) ? (StrokeLineCap) values[5].convert(null) : BorderStrokeStyle.DOTTED == style ? StrokeLineCap.ROUND : StrokeLineCap.BUTT;

                if (styleName != null) {
                    cssStringBuilder.append(styleName);
                } else {
                    if (dashes == null) {
                        throw new NullPointerException("styleName == null && dashes == null (LayeredBorderStyleConverter)");
                    }
                    cssStringBuilder.append("segments(");
                    for (int j = 0; j < dashes.size(); j++) {
                        appendValue(cssStringBuilder, dashes.get(j));
                        if (j != dashes.size() - 1) {
                            cssStringBuilder.append(", ");
                        }
                    }
                    cssStringBuilder.append(")");
                }

                cssStringBuilder
                        .append(" ").append("phase(").append(DF4.format(phase)).append(")")
                        .append(" ");
                appendValue(cssStringBuilder, strokeType);
                cssStringBuilder
                        .append(" ").append("line-join").append(" ");
                appendValue(cssStringBuilder, lineJoin);
                if (lineJoin.equals(StrokeLineJoin.MITER)) {
                    cssStringBuilder.append(" ").append(DF4.format(miterLimit));
                }
                cssStringBuilder.append(" ").append("line-cap").append(" ");
                appendValue(cssStringBuilder, lineCap);

                if (i != sides.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
        CONVERTERS.put(LayeredBackgroundSizeConverter.class, (cssStringBuilder, parsedValue, converter, value) -> {
            ParsedValue[] layeredPositions = (ParsedValue[]) value;
            if (layeredPositions.length == 0) {
                cssStringBuilder.append("null");
            }
            for (int i = 0; i < layeredPositions.length; i++) {
                ParsedValue[] sides = (ParsedValue[]) layeredPositions[i].getValue();
                appendValue(cssStringBuilder, sides[i] == null ? "auto" : sides[i].getValue());
                cssStringBuilder.append(" ");
                appendValue(cssStringBuilder, sides[i] == null ? "auto" : sides[i].getValue());
                cssStringBuilder.append(" ");
                appendValue(cssStringBuilder, sides[i] == null ? "false" : sides[i].getValue());
                cssStringBuilder.append(" ");
                appendValue(cssStringBuilder, sides[i] == null ? "false" : sides[i].getValue());
                if (i != layeredPositions.length - 1) {
                    cssStringBuilder.append(", ");
                }
            }
        });
    }

    static {
        try {
            Class namedColorsClass = Color.class.getClassLoader().loadClass(Color.class.getName() + "$NamedColors");
            Field namedColorsField = namedColorsClass.getDeclaredField("namedColors");
            namedColorsField.setAccessible(true);
            PREDEFINED_COLORS = ((Map<String, Color>) namedColorsField.get(null)).entrySet().stream().collect(Collectors.toMap(x -> x.getValue(), x -> x.getKey(), (x, a) -> x));
        } catch (ReflectiveOperationException e) {
        }

        registerConverters();
    }

    private static void appendSimpleSelector(StringBuilder cssStringBuilder, SimpleSelector selector) {
        String name = ((SimpleSelector) selector).getName();
        String id = ((SimpleSelector) selector).getId();
        if (name == null || name.equals("*")) {
            name = "";
        }
        cssStringBuilder.append(name);
        selector.getStyleClasses().forEach((className) -> {
            cssStringBuilder.append(".").append(className);
        });
        if (id != null && !id.isEmpty()) {
            cssStringBuilder.append("#").append(id);
        }
        selector.getPseudoclasses().forEach((className) -> {
            cssStringBuilder.append(":").append(className);
        });
    }

    private static void appendColor(StringBuilder cssStringBuilder, Color color) {
        if (PREDEFINED_COLORS.containsKey(color)) {
            cssStringBuilder.append(PREDEFINED_COLORS.get(color));
        } else {
            cssStringBuilder.append("#")
                    .append(color.getOpacity() == 1.0 ? "" : String.format("%02x", (int) Math.round(color.getOpacity() * 255)))
                    .append(String.format("%02x", (int) Math.round(color.getRed() * 255)))
                    .append(String.format("%02x", (int) Math.round(color.getGreen() * 255)))
                    .append(String.format("%02x", (int) Math.round(color.getBlue() * 255)));
        }
    }

    private static void appendValue(StringBuilder cssStringBuilder, Object value) {
        if (value instanceof Color) {
            appendColor(cssStringBuilder, (Color) value);
        } else if (value != null && value.getClass().isEnum()) {
            cssStringBuilder.append(value.toString().toLowerCase(Locale.ROOT).replace("_", "-"));
        } else if (value instanceof Size && ((Size) value).getValue() == 0.0) {
            cssStringBuilder.append("0");
        } else {
            cssStringBuilder.append(value);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("JavaFX BSS decompiler by radioegor146");
        if (args.length < 2) {
            System.err.println("Usage: java -jar bss2css.jar <input bss path> <output css path>");
            return;
        }

        Stylesheet stylesheet = Stylesheet.loadBinary(Paths.get(args[0]).toUri().toURL());
        long startTime = System.currentTimeMillis();

        StringBuilder cssStringBuilder = new StringBuilder();
        cssStringBuilder.append("/* origin: ").append(stylesheet.getOrigin()).append(" */\n\n");
        stylesheet.getRules().stream().forEachOrdered((rule) -> {
            for (int i = 0; i < rule.getSelectors().size(); i++) {
                Selector selector = rule.getUnobservedSelectorList().get(i);
                if (selector instanceof SimpleSelector) {
                    appendSimpleSelector(cssStringBuilder, (SimpleSelector) selector);
                } else if (selector instanceof CompoundSelector) {
                    for (int j = 0; j < ((CompoundSelector) selector).getSelectors().size(); j++) {
                        appendSimpleSelector(cssStringBuilder, ((CompoundSelector) selector).getSelectors().get(j));
                        if (j != ((CompoundSelector) selector).getSelectors().size() - 1) {
                            Combinator relationship = ((CompoundSelector) selector).getRelationships().get(j);
                            switch (relationship) {
                                case CHILD:
                                    cssStringBuilder.append(" > ");
                                    break;
                                case DESCENDANT:
                                    cssStringBuilder.append(" ");
                                    break;
                            }
                        }
                    }
                } else {
                    cssStringBuilder.append(selector);
                }

                if (i != rule.getSelectors().size() - 1) {
                    cssStringBuilder.append(", ");
                }
            }
            cssStringBuilder.append(" {\n");
            rule.getUnobservedDeclarationList().forEach(declaration -> {
                cssStringBuilder.append("    ").append(declaration.getProperty()).append(": ");

                StyleConverter converter = declaration.getParsedValue().getConverter();
                Object value = declaration.getParsedValue().getValue();
                Class converterClass = converter == null ? null : converter.getClass();

                if (CONVERTERS.containsKey(converterClass)) {
                    CONVERTERS.get(converterClass).process(cssStringBuilder, declaration.getParsedValue(), converter, value);
                } else {
                    throw new RuntimeException(String.format("Converter '%s' not found (contact author and create issue on github with class name)",
                            converterClass == null ? "null" : converterClass.getName()));
                }

                cssStringBuilder.append(";\n");
            });
            cssStringBuilder.append("}\n\n");
        });
        System.out.println(String.format("Decompiled successfully in %d ms", System.currentTimeMillis() - startTime));

        Files.write(Paths.get(args[1]), cssStringBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }
}
