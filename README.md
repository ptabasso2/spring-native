
# Spring Native Application with GraalVM

## 1. Introduction

This project showcases a basic Spring Boot application configured to build and run as a native image using GraalVM. 

The application includes a `PerfController` that exposes endpoints to simulate CPU and memory-intensive operations, enabling performance profiling and monitoring through Datadog’s continuous profiling tools. 

The primary goal of this project is to create a highly optimized executable, leveraging GraalVM's native image capabilities for improved startup time and reduced resource consumption.

---

## 2. Project Structure

The project follows a standard Maven-based Spring Boot application structure:

```
.
├── Dockerfile                     # Instructions for containerizing the application
├── README.md                      # Documentation
├── dd-java-agent.jar              # Datadog Java agent for profiling
├── docker-compose.yml             # Orchestration for running services
├── mvnw                           # Maven wrapper script
├── pom.xml                        # Maven configuration and build file
├── src
│   └── main
│       ├── java
│       │   └── com.datadoghq.pej.spring_native
│       │       ├── PerfController.java       # REST controller for performance simulations
│       │       └── SpringNativeApplication.java # Main class for the Spring Boot application
│       └── resources
│           └── application.properties       # Configuration properties for the application
```

---

## 3. Controller Source Code Explanation

**Source Code: PerfController.java**

```java
@RestController
public class PerfController {

    @GetMapping("/cpu")
    public String simulateCpuIssue() {
        int[] dataset = new int[1000];
        for (int i = 0; i < dataset.length; i++) {
            dataset[i] = i;
        }
        int targetSum = 1000;
        int count = findTripletsWithSum(dataset, targetSum);
        return "Task completed! Triplets found: " + count;
    }

    private int findTripletsWithSum(int[] arr, int target) {
        int count = 0;
        int n = arr.length;
        for (int i = 0; i < n - 2; i++) {
            for (int j = i + 1; j < n - 1; j++) {
                for (int k = j + 1; k < n; k++) {
                    if (arr[i] + arr[j] + arr[k] == target) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private final List<Object> memoryHog = new ArrayList<>();

    @GetMapping("/memory")
    public String simulateMemoryIssue() {
        try {
            for (int i = 0; i < 100_000; i++) {
                byte[] chunk = new byte[200_000];
                memoryHog.add(chunk);
            }
        } catch (OutOfMemoryError e) {
            return "Memory issue simulated: " + e.getMessage();
        }
        return "Memory issue simulation completed. Memory usage might be high!";
    }
}
```

**Explanation**:
- **`/cpu` Endpoint**: Simulates CPU-intensive computation by executing an inefficient algorithm to find all triplets in a dataset whose sum matches a target value.
- **`/memory` Endpoint**: Simulates memory pressure by continuously allocating large byte arrays and storing them in memory, potentially causing an `OutOfMemoryError`.

---

## 4. Key Build File (pom.xml) Elements

The `pom.xml` includes key dependencies and plugins to enable building a native image using GraalVM:

### Dependencies
- `spring-boot-starter-web`: Provides web-related functionalities.
- `javax.servlet-api`: Allows servlet-based operations.
- `spring-boot-starter-test`: Enables testing capabilities.

### Plugins
- `native-maven-plugin`: Configures GraalVM native image compilation.
- `spring-boot-maven-plugin`: Supports Spring Boot build lifecycle.

### Native Image Configuration
- **`imageName`**: Specifies the name of the resulting native image.
- **`mainClass`**: Specifies the main class to be used during the build.
- **`jvmArgs`** and **`buildArgs`**: Add profiling and monitoring capabilities.

---

## 5. Building and Testing Locally

To build and test the application locally:

### Prerequisites

* **JDK GraalVM 17 or 21+**: Ensure that JDK is installed on your machine.
* **Maven 3.9.x**: Verify that Maven 3.0+ is installed.
* **Docker**: Install Docker and Docker compose to build and run containerized services.

### Clone de repository

```bash
   git clone https://github.com/ptabasso2/spring-native.git
   cd spring-native
   ```

### Build the Native Image
Ensure GraalVM is installed and set up correctly, then execute the Maven command:
```bash
./mvnw -Pnative native:compile
```

### Start the Datadog Agent
Replace `xxxxxxxx` with your Datadog API key:
   ```bash
   docker run --rm -d --name dd-agent-dogfood-jmx -v /var/run/docker.sock:/var/run/docker.sock:ro -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro -p 8126:8126 -p 8125:8125/udp -e DD_API_KEY=xxxxxxxx -e DD_APM_ENABLED=true -e DD_APM_NON_LOCAL_TRAFFIC=true -e DD_PROCESS_AGENT_ENABLED=true -e DD_DOGSTATSD_NON_LOCAL_TRAFFIC="true" -e DD_LOG_LEVEL=debug -e DD_LOGS_ENABLED=true -e DD_LOGS_CONFIG_CONTAINER_COLLECT_ALL=true -e DD_CONTAINER_EXCLUDE_LOGS="name:datadog-agent" gcr.io/datadoghq/agent:latest-jmx
   ```

### Run the Native Executable
After building, the executable can be found in the `target` directory. Run the application:
```bash
./target/spring-native
```

The console should display the following:
```bash
./target/spring-native

.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/

:: Spring Boot ::                (v3.4.0)

2024-12-28T10:04:07.148Z  INFO 2231669 --- [spring-native] [           main] c.d.p.s.SpringNativeApplication          : Starting AOT-processed SpringNativeApplication using Java 21.0.5 with PID 2231669 (/root/spring-native/spring-native/target/spring-native started by root in /root/spring-native/spring-native)
2024-12-28T10:04:07.148Z  INFO 2231669 --- [spring-native] [           main] c.d.p.s.SpringNativeApplication          : No active profile set, falling back to 1 default profile: "default"
2024-12-28T10:04:07.174Z  INFO 2231669 --- [spring-native] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-12-28T10:04:07.176Z  INFO 2231669 --- [spring-native] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-12-28T10:04:07.176Z  INFO 2231669 --- [spring-native] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.33]
2024-12-28T10:04:07.199Z  INFO 2231669 --- [spring-native] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-12-28T10:04:07.199Z  INFO 2231669 --- [spring-native] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 51 ms
2024-12-28T10:04:07.267Z  INFO 2231669 --- [spring-native] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2024-12-28T10:04:07.268Z  INFO 2231669 --- [spring-native] [           main] c.d.p.s.SpringNativeApplication          : Started SpringNativeApplication in 0.139 seconds (process running for 0.203)
[dd.trace 2024-12-28 10:04:07:626 +0000] [dd-task-scheduler] INFO datadog.trace.agent.core.StatusLogger - DATADOG TRACER CONFIGURATION {"version":"1.44.1~13a9a2d011","os_name":"Linux","os_version":"6.8.0-1011-gcp","architecture":"amd64","lang":"jvm","lang_version":"21.0.5","jvm_vendor":"Oracle Corporation","jvm_version":"21.0.5+9-LTS","java_class_version":"65.0","http_nonProxyHosts":"null","http_proxyHost":"null","enabled":true,"service":"org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGeneratorRunner","agent_url":"http://localhost:8126","agent_error":true,"debug":false,"trace_propagation_style_extract":["datadog","tracecontext"],"trace_propagation_style_inject":["datadog","tracecontext"],"analytics_enabled":false,"priority_sampling_enabled":true,"logs_correlation_enabled":true,"profiling_enabled":true,"remote_config_enabled":true,"debugger_enabled":false,"debugger_exception_enabled":false,"debugger_span_origin_enabled":false,"appsec_enabled":"FULLY_DISABLED","rasp_enabled":true,"telemetry_enabled":false,"telemetry_dependency_collection_enabled":true,"telemetry_log_collection_enabled":false,"dd_version":"","health_checks_enabled":true,"configuration_file":"no config file present","runtime_id":"becbf736-41c7-4798-9a39-93ffb3e45267","logging_settings":{"levelInBrackets":false,"dateTimeFormat":"'[dd.trace 'yyyy-MM-dd HH:mm:ss:SSS Z']'","logFile":"System.err","configurationFile":"simplelogger.properties","showShortLogName":false,"showDateTime":true,"showLogName":true,"showThreadName":true,"defaultLogLevel":"INFO","warnLevelString":"WARN","embedException":false},"cws_enabled":false,"cws_tls_refresh":5000,"datadog_profiler_enabled":false,"datadog_profiler_safe":false,"datadog_profiler_enabled_overridden":false,"data_streams_enabled":false}
```

### Test Endpoints

Run the following `curl` commands to test the service:

- To simulate CPU load

```bash
curl localhost:8080/cpu
```

- To simulate memory load

```bash
curl localhost:8080/memory
```


---

## 6. Building and Testing with Docker

### Dockerfile
```dockerfile
# Stage 1: Build the Spring Boot application
FROM ghcr.io/graalvm/native-image-community:21.0.0 AS build

COPY --from=maven:3.9.9-eclipse-temurin-17 /usr/share/maven /usr/share/maven

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the Spring Boot application
RUN /usr/share/maven/bin/mvn -Pnative native:compile

# Stage 2: Run the spring native application.
# Just use something small with glibc and curl. ubuntu:22.04 ships no curl, rockylinux:9 does.
# This avoids apt-get update/install, which leads to flakiness on mirror upgrades.
FROM rockylinux:9

# Set the working directory
WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/target/spring-native spring-native

# Expose the application port
EXPOSE 8080

RUN chmod +x spring-native

# Command to run the application with the Datadog Java Agent
CMD ["./spring-native"]
```

### docker-compose.yml
```yaml
version: '3.5'

services:
  dd-agent:
    container_name: dd-agent
    image: gcr.io/datadoghq/agent:latest-jmx
    environment:
      - DD_HOSTNAME=datadog
      - DD_API_KEY
      - DD_APM_ENABLED=true
      - DD_APM_NON_LOCAL_TRAFFIC=true
      - DD_PROCESS_AGENT_ENABLED=true
      - DD_DOGSTATSD_NON_LOCAL_TRAFFIC="true"
      - DD_LOG_LEVEL=debug
      - DD_LOGS_ENABLED=true
      - DD_LOGS_CONFIG_CONTAINER_COLLECT_ALL=true
      - DD_CONTAINER_EXCLUDE_LOGS="name:datadog-agent"
      - SD_JMX_ENABLE=true
    ports:
      - "8125:8125"
      - "8126:8126"
    volumes:
      - /proc/:/host/proc/:ro
      - /sys/fs/cgroup/:/host/sys/fs/cgroup:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    networks:
      - app

  springnative:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: springnative
    hostname: springnative
    environment:
      - DD_AGENT_HOST=dd-agent
      - DD_SERVICE=springnative
      - DD_ENV=dev
      - DD_VERSION=1.2
    ports:
      - "8080:8080"
    networks:
      - app

networks:
  app:
    driver: bridge
    name: app
```

### Build and run the Docker Image

Replace `xxxxxxxx` with your Datadog API key:

   ```bash
    docker-compose build
    DD_API_KEY=xxxxxxxx docker-compose up -d
   ```
Or in one pass

   ```bash
    DD_API_KEY=xxxxxxxx docker-compose up --build -d
   ```

You should normally see both services up and running:
   ```bash
docker-compose ps
Name            Command            State                                                  Ports
-----------------------------------------------------------------------------------------------------------------------------------------------
dd-agent       /bin/entrypoint.sh   Up (healthy)   0.0.0.0:8125->8125/tcp,:::8125->8125/tcp, 8125/udp, 0.0.0.0:8126->8126/tcp,:::8126->8126/tcp
springnative   ./spring-native      Up             0.0.0.0:8080->8080/tcp,:::8080->8080/tcp
   ```

### Test in Docker
- Use the same endpoints to test the application:
    - `http://localhost:8080/cpu`
    - `http://localhost:8080/memory`

---

