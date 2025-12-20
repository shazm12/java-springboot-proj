package com.pm.stack;

import software.amazon.awscdk.Stack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.protocol.Protocol;
import com.amazonaws.services.appfabric.model.Credential;
import com.amazonaws.services.cognitoidentity.model.Credentials;
import com.amazonaws.services.ecs.model.Cluster;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.PortMapping;
import com.amazonaws.services.logs.model.LogGroup;

import software.amazon.awscdk.App;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.docdb.DatabaseInstance;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.CloudMapNamespaceOptions;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.FargateTaskDefinition;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

public class LocalStack extends Stack {

    private final Vpc vpc;

    private final Cluster ecsCluster;

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.vpc = createVpc();
        this.ecsCluster = createEcsCluster();
        DatabaseInstance authServiceDB = createDatabase("AuthServiceDB", "auth-service-db");
        DatabaseInstance patientServiceDB = createDatabase("PatientServiceDB", "patient-service-db");

        CfnHealthCheck authDbCfnHealthCheck = createDbHealthCheck(authServiceDB, "AuthDbHealthCheck");
        CfnHealthCheck patientDbCfnHealthCheck = createDbHealthCheck(patientServiceDB, "PatientDbHealthCheck");

        CfnCluster mskCluster = createMskCluster();

        FargateService authService = createFargateService("AuthService", "auth-service-image",
                List.of(4005), authServiceDB, Map.of("JWT_SECERET", "secert"));

        authService.getNode().addDependency(authDbCfnHealthCheck);
        authService.getNode().addDependency(authServiceDB);

    }

    private Vpc createVpc() {
        return Vpc.Builder.create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabase(String id, String dbName) {
        return DatabaseInstance.Builder.create(this, id)
                .engine(DatabaseInstanceEngine
                        .postgres(PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2))
                        .build())
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .datbaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private cfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster createMskCluster() {
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(
                        CfnCluster.BrokerNodeGroupInfoProperty.builder().instanceType("kafka.m5.xlarge")
                                .clientSubnets(
                                        (vpc.getPrivateSubnets().stream().map(ISubnet::getSubnetId)
                                                .collect(Collectors.toList())))
                                .brokerAzDistribution("DEFAULT")
                                .build())
                .build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "PatientManagementEcsCluster")
                .clusterName("PatientManagementEcsCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder().name("patientmanagement.local").build())
                .build();
    }

    private FargateService createFargateService(String id, String imageName, List<Integer> ports, DatabaseInstance db,
            Map<String, String> additionalEnvVars) {

        FargateTaskDefinition taskDefinition = FargateTaskDefination.Builder.create(this, id + "Task")

                .cpu(256)
                .memeoryLimitMiB(512)

                .build();

        ContainerDefinitionOptions.Builder containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports.stream().map(port -> PortMapping.builder()
                        .containerPort(port).hostPort(port).protocol().build(Protocol.TCP).toList()))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Buidler.create(this, id + "LogGroup").logGroupName("/ecs/" + imageName)
                                .removalPolicy(RemovalPolicy.DESTROY).retention(RetentionDays.ONE_DAY).build())
                        .build()));

        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510",
                "localhost.localstack.cloud:4511", "localhost.localstack.cloud:4512");

        if (addtionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        if (db != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db"
                    .formatted(db.getDbInstanceEndpointAddress(), db.getDbInstanceEndpointPort(), imageName));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        containerDefinitionOptions.environment(envVars);
        taskDefinition.addContainer(imageName + "Container", containerDefinitionOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefination(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();

    }

    public static void main(final String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        StackProps stackProps = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new LocalStack(app, "localstack", stackProps);

        app.synth();

        System.out.println("App synthesizing in progress...");

    }

}
