// Autor:  Oscar Ruiz Salinas
// Fecha: Mayo 205
// Descripción:  Permite generar el reporte de los componentes que residen en una base de datos Postgresql
import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ReportePostgreSQL {

    private static final String CONFIG_FILE = "config.properties";
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String RECURSO;
    private static final String HTML_FILE_PREFIX = "reporte_postgresql_";
    private static final String HTML_FILE_EXTENSION = ".html";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        try {
            leerConfiguracion();
            leerPassword();
            String htmlContent = generarReporteHTML();
            String nombreArchivoSalida = generarNombreArchivoHTML();
            escribirArchivoHTML(htmlContent, nombreArchivoSalida);
            System.out.println("Reporte HTML generado exitosamente en: " + nombreArchivoSalida);
        } catch (SQLException | IOException e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
        }
    }

    private static void leerConfiguracion() throws IOException {
        Properties config = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            config.load(reader);
            DB_URL = config.getProperty("db.url");
            DB_USER = config.getProperty("db.user");
            RECURSO = config.getProperty("recurso", "");
            if (DB_URL == null || DB_USER == null) {
                throw new IOException("Faltan parámetros de conexión en el archivo: " + CONFIG_FILE);
            }
        }
    }

    private static void leerPassword() {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword("Introduce la contraseña de la base de datos: ");
            DB_PASSWORD = new String(passwordArray);
        } else {
            System.err.println("Advertencia: No se pudo acceder a la consola para entrada segura de la contraseña.");
            System.err.print("Introduce la contraseña de la base de datos: ");
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            DB_PASSWORD = scanner.nextLine();
        }
    }

    private static String generarNombreArchivoHTML() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        StringBuilder nombreArchivo = new StringBuilder(HTML_FILE_PREFIX);
        nombreArchivo.append(formatter.format(now));
        if (!RECURSO.isEmpty()) {
            nombreArchivo.append("_").append(RECURSO);
        }
        nombreArchivo.append(HTML_FILE_EXTENSION);
        return nombreArchivo.toString();
    }

    private static String generarReporteHTML() throws SQLException, IOException {
        StringBuilder html = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        String fechaHoraActual = now.format(DATE_TIME_FORMATTER);

        // Encabezado HTML
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"es\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>Reporte de Base de Datos PostgreSQL</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: sans-serif; margin: 20px; }\n");
        html.append("h1, h2 { color: #333; }\n");
        html.append(".resumen { border: 1px solid #ccc; padding: 15px; margin-bottom: 20px; background-color: #f9f9f9; }\n");
        html.append(".lista { border: 1px solid #ccc; margin-bottom: 15px; }\n");
        html.append(".lista h3 { background-color: #eee; padding: 8px; margin: 0; }\n");
        html.append(".lista ul { list-style: none; padding: 0; margin: 0; }\n");
        html.append(".lista li { padding: 8px; border-bottom: 1px solid #eee; }\n");
        html.append(".lista li:last-child { border-bottom: none; }\n");
        html.append("table { width: 100%; border-collapse: collapse; }\n");
        html.append("th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #eee; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<h1>Reporte de Base de Datos PostgreSQL</h1>\n");

        // Resumen
        html.append("<div class=\"resumen\">\n");
        html.append("<h2>Resumen</h2>\n");
        html.append("<p><strong>Fecha y Hora:</strong> ").append(fechaHoraActual).append("</p>\n");
        html.append("<p><strong>Dirección de la Base de Datos:</strong> ").append(obtenerDireccionBaseDatos()).append("</p>\n");
        html.append("<p><strong>Base de Datos:</strong> ").append(obtenerNombreBaseDatos()).append("</p>\n");
        html.append("<p><strong>Total de Tablas:</strong> ").append(contarTablas()).append("</p>\n");
        html.append("<p><strong>Total de Funciones:</strong> ").append(contarFunciones()).append("</p>\n");
        html.append("<p><strong>Total de Procedimientos Almacenados:</strong> ").append(contarProcedimientosAlmacenados()).append("</p>\n");
        html.append("<p><strong>Total de Triggers:</strong> ").append(contarTriggers()).append("</p>\n");
        html.append("<p><strong>Total de Usuarios:</strong> ").append(contarUsuarios()).append("</p>\n");
        html.append("</div>\n");

        // Listado de Tablas y Total de Renglones
        html.append("<div class=\"lista\">\n");
        html.append("<h3>Listado de Tablas y Total de Renglones</h3>\n");
        html.append("<table>\n");
        html.append("<thead><tr><th>Nombre de la Tabla</th><th>Total de Renglones</th></tr></thead>\n");
        html.append("<tbody>\n");
        obtenerTotalRenglonesTablas().forEach(item -> {
            html.append("<tr><td>").append(item.nombreTabla).append("</td><td>").append(item.totalRenglones).append("</td></tr>\n");
        });
        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        // Listado de Funciones
        html.append("<div class=\"lista\">\n");
        html.append("<h3>Listado de Funciones</h3>\n");
        html.append("<ul>\n");
        obtenerNombresFunciones().forEach(nombre -> html.append("<li>").append(nombre).append("</li>\n"));
        html.append("</ul>\n");
        html.append("</div>\n");

        // Listado de Procedimientos Almacenados
        html.append("<div class=\"lista\">\n");
        html.append("<h3>Listado de Procedimientos Almacenados</h3>\n");
        html.append("<ul>\n");
        obtenerNombresProcedimientosAlmacenados().forEach(nombre -> html.append("<li>").append(nombre).append("</li>\n"));
        html.append("</ul>\n");
        html.append("</div>\n");

        // Listado de Triggers
        html.append("<div class=\"lista\">\n");
        html.append("<h3>Listado de Triggers</h3>\n");
        html.append("<ul>\n");
        obtenerNombresTriggers().forEach(nombre -> html.append("<li>").append(nombre).append("</li>\n"));
        html.append("</ul>\n");
        html.append("</div>\n");

        // Listado de Usuarios
        html.append("<div class=\"lista\">\n");
        html.append("<h3>Listado de Usuarios</h3>\n");
        html.append("<ul>\n");
        obtenerNombresUsuarios().forEach(nombre -> html.append("<li>").append(nombre).append("</li>\n"));
        html.append("</ul>\n");
        html.append("</div>\n");

        // Fin del HTML
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private static String obtenerDireccionBaseDatos() {
        if (DB_URL != null && DB_URL.startsWith("jdbc:postgresql://")) {
            String[] parts = DB_URL.substring("jdbc:postgresql://".length()).split("/");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return "No se pudo obtener la dirección de la base de datos";
    }

    private static String obtenerNombreBaseDatos() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_database()")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return "No se pudo obtener el nombre de la base de datos";
    }

    private static int contarTablas() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static int contarFunciones() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pg_proc WHERE pronamespace = 'public'::regnamespace")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static int contarProcedimientosAlmacenados() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pg_proc WHERE prokind = 'p' AND pronamespace = 'public'::regnamespace")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static int contarTriggers() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_catalog = current_catalog AND trigger_schema = 'public'")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static int contarUsuarios() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pg_catalog.pg_user")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static List<TablaConRenglones> obtenerTotalRenglonesTablas() throws SQLException {
        List<TablaConRenglones> resultados = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")) {
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                int rowCount = obtenerTotalRenglonesTabla(conn, tableName);
                resultados.add(new TablaConRenglones(tableName, rowCount));
            }
        }
        return resultados;
    }

    private static int obtenerTotalRenglonesTabla(Connection conn, String tableName) throws SQLException {
        try (Statement countStmt = conn.createStatement();
             ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (countRs.next()) {
                return countRs.getInt(1);
            }
        }
        return 0;
    }

    private static List<String> obtenerNombresFunciones() throws SQLException {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT proname FROM pg_proc WHERE pronamespace = 'public'::regnamespace AND prokind = 'f'")) {
            while (rs.next()) {
                nombres.add(rs.getString("proname"));
            }
        }
        return nombres;
    }

    private static List<String> obtenerNombresProcedimientosAlmacenados() throws SQLException {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT proname FROM pg_proc WHERE pronamespace = 'public'::regnamespace AND prokind = 'p'")) {
            while (rs.next()) {
                nombres.add(rs.getString("proname"));
            }
        }
        return nombres;
    }

    private static List<String> obtenerNombresTriggers() throws SQLException {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT trigger_name FROM information_schema.triggers WHERE trigger_catalog = current_catalog AND trigger_schema = 'public'")) {
            while (rs.next()) {
                nombres.add(rs.getString("trigger_name"));
            }
        }
        return nombres;
    }

    private static List<String> obtenerNombresUsuarios() throws SQLException {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT usename FROM pg_catalog.pg_user")) {
            while (rs.next()) {
                nombres.add(rs.getString("usename"));
            }
        }
        return nombres;
    }

    private static void escribirArchivoHTML(String content, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
        }
    }

    private static class TablaConRenglones {
        String nombreTabla;
        int totalRenglones;

        public TablaConRenglones(String nombreTabla, int totalRenglones) {
            this.nombreTabla = nombreTabla;
            this.totalRenglones = totalRenglones;
        }
    }
}
