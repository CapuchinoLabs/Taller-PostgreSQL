# Reporte de base de datos Postgresql

### Descripción
Este código Java permite generar un reporte acerca de los componentes que residen en una base de datos Postgresql.

Toma como entrada un archivo de propiedades llamado config.properties con los parámetros de conexión hacia Postgres.

Al momento de ejecutar la aplicación solicitará el password, establece la conexión hacia la base de datos y genera un reporte HTML 
con la información obtenida.

### Requerimientos 
JDK 1.8 o superior
JDBC driver para PostgreSQL
  https://jdbc.postgresql.org/download/ 

### Actividades
1. Definimos los parámetros de conexión del servidor origen
```bash
[admon@centos8 ~]$ vi origen
db.url=jdbc:postgresql://10.0.0.4:5432/base01
db.user=user01
recurso=origen
```

2. Definimos los parámetros de conexión del servidor destino
```bash
[admon@centos8 ~]$ vi destino
db.url=jdbc:postgresql://postgresql-01-ors.postgres.database.azure.com:5432/destino01
db.user=user01
recurso=destino
```

3. Compilamos el archivo
```bash
[admon@centos8 ~]$ /usr/local/jdk1.8.0_202/bin/javac ReportePostgreSQL.java
```

4. Generamos el reporte de la base de datos origen
```bash
[admon@centos8 ~]$ cp origen config.properties
[admon@centos8 ~]$ jdk1.8.0_202/bin/java -cp postgresql-42.7.5.jar:. ReportePostgreSQL
```

5. Identificamos el archivo generado: reporte_postgresql_2025XXXX_XXXXX_origen.html
   
6. Realizamos la migración
   
7. Generamos el reporte de la base de datos destino
```bash
   [admon@centos8 ~]$ cp destino config.properties
   [admon@centos8 ~]$ jdk1.8.0_202/bin/java -cp postgresql-42.7.5.jar:. ReportePostgreSQL
```

8. Identificamos el archivo generado: reporte_postgresql_2025XXXX_XXXXX_destino.html

 
