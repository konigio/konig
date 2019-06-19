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

public class AnimalTargetShapeBeam {

    private static String sourceURI(AnimalTargetShapeBeam.Options options) {
        String envName = options.getEnvironment();
        return ("gs://example-inbound-${environmentName}".replace("${environmentName}", options.getEnvironment())+"/*");
    }

    public static void process(AnimalTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        String sourceURI = sourceURI(options);
        String pattern = "gs://example-inbound-${environmentName}/invalid/{0}".replace("${environmentName}", options.getEnvironment());
        PCollectionTuple outputTuple = p.apply(org.apache.beam.sdk.io.FileIO.match().filepattern(sourceURI)).apply(org.apache.beam.sdk.io.FileIO.readMatches()).apply("ReadFiles", ParDo.of(new ReadAnimalSourceShapeFn()).withOutputTags(ReadAnimalSourceShapeFn.successTag, TupleTagList.of(ReadAnimalSourceShapeFn.deadLetterTag)));
        outputTuple.get(ReadAnimalSourceShapeFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", org.apache.beam.sdk.io.FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.ns.shape.animal")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> org.apache.beam.sdk.io.FileIO.Write.defaultNaming(("file-"+ key), ".csv")));
        PCollectionTuple outputTuple2 = outputTuple.get(ReadAnimalSourceShapeFn.successTag).apply("ToAnimalTargetShape", ParDo.of(new ToAnimalTargetShapeFn()).withOutputTags(ToAnimalTargetShapeFn.successTag, TupleTagList.of(ToAnimalTargetShapeFn.deadLetterTag)));
        outputTuple2 .get(ToAnimalTargetShapeFn.successTag).apply("WriteAnimalTargetShape", BigQueryIO.writeTableRows().to("ex.AnimalTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputTuple2 .get(ToAnimalTargetShapeFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", org.apache.beam.sdk.io.FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.ns.shape.animal")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> org.apache.beam.sdk.io.FileIO.Write.defaultNaming(("file-"+ key), ".txt")));
        p.run();
    }

    public static void main(String[] args) {
        AnimalTargetShapeBeam.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(AnimalTargetShapeBeam.Options.class);
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
