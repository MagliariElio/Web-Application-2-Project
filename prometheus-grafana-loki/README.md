# Guide to Starting and Verifying Microservices and the Monitoring System (Prometheus, Grafana, Loki)

## 1. Starting Microservices
- Start all microservices: **CRM**, **Communication Manager**, **Document Store**, **Analytics**, and the **gateway** (Client-Gateway).
- Launch the corresponding Docker containers for each microservice.

## 2. Starting the Prometheus-Grafana-Loki System
- Start the Docker containers for **Prometheus**, **Grafana**, and **Loki** included in the project.

## 3. Verifying Prometheus
- Open Prometheus **targets** page: [http://localhost:9091/targets](http://localhost:9091/targets).
- If the microservices are visible with "UP" status, Prometheus is working correctly and is communicating with the services.
- If any services are **DOWN**:
    - Wait at least 1 minute after starting them.
    - Check if the services are active and analyze the error messages displayed to resolve issues.

## 4. Verifying Loki
- Open the **Loki** metrics page: [http://localhost:3100/metrics](http://localhost:3100/metrics).
- If you get a response, Loki has been initialized successfully.
- If not, ensure the Loki Docker container is running and resolve any configuration issues.

## 5. Verifying Grafana
- Access **Grafana** at [http://localhost:3000](http://localhost:3000).
- Login with:
    - **Username**: `admin`
    - **Password**: `admin`
- If prompted to change the password, re-enter `admin`.
- After login:
    - Go to **Connections > Data Sources** from the sidebar and verify that **Prometheus** and **Loki** are configured.
    - Test the connection for both datasources by scrolling to the bottom and clicking "Test". If the response is positive, everything is set up correctly.
    - Go to **Dashboards** from the sidebar and ensure the dashboards are visible with the correct metrics. **Each chart has its own description**.