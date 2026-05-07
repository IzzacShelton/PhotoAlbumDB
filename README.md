# PhotoAlbumDB
## Running / Building the Application 

**Prerequisites:**
* Docker
* Java 21 (JDK)
* Maven

### 1. Start the Database
The project uses Docker to spin up a MySQL 8.0 instance locally. On the first run, it will automatically execute the initialization scripts in the `sql_scripts` folder to build the database schema.
```bash
docker compose up -d
```
> Note: Especially after the first time composing the container, you may need need to wait for the MySQL server to initialize and start up before launching the application. 
### 2. Run the Application
You can launch the JavaFX application directly from the source code using the Maven JavaFX plugin:
```bash
mvn javafx:run
```

### 3. Build an Executable (Optional)
To bundle the application and all its dependencies into a single executable JAR:
```bash
mvn clean package
```

The compiled JAR will be generated in the `/target` directory and can be run using:
```bash
java -jar target/AlbumApp-1.0-SNAPSHOT.jar
```



