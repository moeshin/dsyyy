package site.littlehands.dsyyy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SettingUtils {

    static final String KEY_PATH = "path";
    static final String KEY_FORMAT = "format";

    private static final String MARK_SONG = "%name%";
    private static final String MARK_AUTHOR = "%artist%";
    private static final String MARK_ALBUM = "%album%";

    private static final Pattern PATTERN = Pattern.compile("%[a-z]*%");

    static String format(String format, String song, String author, String album) {

        Matcher matcher = PATTERN.matcher(format);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String mark = matcher.group();
            String replacement;
            switch (mark) {
                case MARK_SONG:
                    replacement = song;
                    break;
                case MARK_AUTHOR:
                    replacement = author;
                    break;
                case MARK_ALBUM:
                    replacement = album;
                    break;
                default:
                    continue;
            }
            matcher.appendReplacement(buffer, replacement);
        }
        return buffer.toString();
    }
}
