package com.pm.stack;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.AppProps;

public class LocalStack extends Stack {

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
    }

    public static void main(final String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        StackProps stackProps = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new LocalStack(app, "localstack", stackProps);

        app.synth();

        System.out.println("App synthesizing in progress...");

    }

}
