import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportePostgreSQL {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/tu_base_de_datos";
    private static final String DB_USER = "tu_usuario";
    private static final String DB_PASSWORD = "tu_contraseña";
    private static final String HTML_FILE = "reporte_postgresql.html";

    public static void main(String[] args) {
        try {
            String htmlContent = generarReporteHTML();
            escribirArchivoHTML(htmlContent);
            System.out.println("Reporte HTML generado exitosamente en: " + HTML_FILE);
        } catch (SQLException | IOException | UnknownHostException e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
        }
    }

    private static String generarReporteHTML() throws SQLException, IOException, UnknownHostException {
        StringBuilder html = new StringBuilder();

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
        html.append("th { background-color: #
