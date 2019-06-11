package com.example.beam.etl.shape;

import java.text.MessageFormat;
import java.util.UUID;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;

public class PersonTargetShapeBeam {

    private static String sourceURI(PersonTargetShapeBeam.Options options) {
        String envName = options.getEnvironment();
        return ("gs://example-inbound-${environmentName}".replace("${environmentName}", options.getEnvironment())+"/*");
    }

    public static void process(PersonTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        String sourceURI = sourceURI(options);
        String pattern = "gs://example-inbound-${environmentName}/invalid/{0}".replace("${environmentName}", options.getEnvironment());
        PCollectionTuple outputTuple = p.apply(org.apache.beam.sdk.io.FileIO.match().filepattern(sourceURI)).apply(org.apache.beam.sdk.io.FileIO.readMatches()).apply("ReadFiles", ParDo.of(new ReadPersonSourceShapeFn()).withOutputTags(ReadPersonSourceShapeFn.successTag, TupleTagList.of(ReadPersonSourceShapeFn.deadLetterTag)));
        outputTuple.setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", org.apache.beam.sdk.io.FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply() {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.ns.shape.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> org.apache.beam.sdk.io.FileIO.Write.defaultNaming(key, ".csv")));
        PCollectionTuple outputTuple2 = outputTuple.get(ReadPersonSourceShapeFn.successTag).apply("ToPersonTargetShape", ParDo.of(new ToPersonTargetShapeFn()).withOutputTags(ToPersonTargetShapeFn.successTag, TupleTagList.of(ToPersonTargetShapeFn.deadLetterTag)));
        outputTuple2 .get(ToPersonTargetShapeFn.successTag).apply("WritePersonTargetShape", BigQueryIO.writeTableRows().to("schema.PersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputTuple2 .setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", org.apache.beam.sdk.io.FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply() {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.ns.shape.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> org.apache.beam.sdk.io.FileIO.Write.defaultNaming(key, ".txt")));
        p.run();
    }

    public static void main(String[] args) {
        PersonTargetShapeBeam.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(PersonTargetShapeBeam.Options.class);
        process(options);
    }

    public interface Options
        extends PipelineOptions
    {

        @Validation.Required
        @Description("The name of the environment; typically one of (dev, test, stage, prod)")
        public String getEnvironment();

        public void setEnvironment(String envName);
    }
}
