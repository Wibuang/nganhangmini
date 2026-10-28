package core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Ghi file CSV mở được bằng Excel (UTF-8 có BOM nên không lỗi tiếng Việt). */
public class CsvUtil {

    /** Mỗi phần tử của dong là một dòng, mỗi String là một ô. */
    public static void ghi(File file, List<String[]> dong) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');   // BOM để Excel đọc đúng UTF-8
            for (String[] d : dong) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < d.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(bocO(d[i]));
                }
                w.write(sb.append("\r\n").toString());
            }
        }
    }

    // Bọc giá trị trong dấu nháy kép, nháy kép bên trong được nhân đôi
    private static String bocO(String s) {
        String x = s == null ? "" : s;
        return "\"" + x.replace("\"", "\"\"") + "\"";
    }
}
