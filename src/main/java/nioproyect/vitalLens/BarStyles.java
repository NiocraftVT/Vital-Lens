package nioproyect.vitalLens;

public class BarStyles {

    public static String[] getStyle(String style) {

        switch (style.toLowerCase()) {

            case "grapes":
                return new String[]{"●","○"};

            case "orion":
                return new String[]{"✦","✧"};

            case "masaru":
                return new String[] {"⭐","☆"};

            case "inari":
                return new String[] {"\uD83E\uDD8A","\uD83D\uDC3E"};

            case "valencho":
                return new String[] {"\uD83D\uDC09","\uD83D\uDC09"};

            case "nixcel":
                return new String[] {"\uD83D\uDD8C","☆"};

            case "crisneko":
                return new String[] {"\uD83D\uDC08","\uD83D\uDC3E"};

            case "default":
            default:
                return new String[]{"█","░"};
        }
    }
}
