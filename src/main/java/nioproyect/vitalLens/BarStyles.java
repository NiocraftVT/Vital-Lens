package nioproyect.vitalLens;

public class BarStyles {

    public static String[] getStyle(String style) {

        switch (style.toLowerCase()) {

            case "grapes":
                return new String[]{"□","▣"};

            case "orion":
                return new String[]{"■","□"};

            case "default":
            default:
                return new String[]{"█","░"};
        }
    }
}
