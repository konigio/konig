package com.example.beam.etl.shape;

import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.TableRowJsonCoder;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.ParDo;
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
        PCollectionTuple outputTuple = p.apply(FileIO.match().filepattern(sourceURI)).apply(FileIO.readMatches()).apply("ReadFiles", ParDo.of(new ReadAnimalSourceShapeFn()).withOutputTags(ReadAnimalSourceShapeFn.successTag, TupleTagList.of(ReadAnimalSourceShapeFn.deadLetterTag)));
        outputTuple.get(ReadAnimalSourceShapeFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WriteAnimalSourceErrorLog", BigQueryIO.writeTableRows().to("ex.ErrorAnimalSource").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        PCollectionTuple outputTuple2 = outputTuple.get(ReadAnimalSourceShapeFn.successTag).apply("ToAnimalTargetShape", ParDo.of(new ToAnimalTargetShapeFn()).withOutputTags(ToAnimalTargetShapeFn.successTag, TupleTagList.of(ToAnimalTargetShapeFn.deadLetterTag)));
        outputTuple2 .get(ToAnimalTargetShapeFn.successTag).apply("WriteAnimalTargetShape", BigQueryIO.writeTableRows().to("ex.AnimalTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputTuple2 .get(ToAnimalTargetShapeFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WriteAnimalTargetErrorLog", BigQueryIO.writeTableRows().to("ex.ErrorAnimalTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
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

        public String getNetwork();

        public void setNetwork(String gcpNetwork);

        public String getSubnetwork();

        public void setSubnetwork(String subnetwork);

        public String getWorkerMachineType();

        public void setWorkerMachineType(String workerMachineType);

        public String getModifiedDate();

        public void setModifiedDate(String modifiedDate);
    }
}
