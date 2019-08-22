package com.example.beam.etl.shape;

import com.google.api.client.util.DateTime;
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

public class PersonTargetShapeBeam {

    private static String sourceURI(PersonTargetShapeBeam.Options options) {
        String envName = options.getEnvironment();
        return ("gs://example-inbound-${environmentName}".replace("${environmentName}", options.getEnvironment())+"/*");
    }

    public static void process(PersonTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        String sourceURI = sourceURI(options);
        PCollectionTuple outputTuple = p.apply(FileIO.match().filepattern(sourceURI)).apply(FileIO.readMatches()).apply("ReadFiles", ParDo.of(new ReadPersonSourceShapeFn()).withOutputTags(ReadPersonSourceShapeFn.successTag, TupleTagList.of(ReadPersonSourceShapeFn.deadLetterTag)));
        outputTuple.get(ReadPersonSourceShapeFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WritePersonSourceErrorLog", BigQueryIO.writeTableRows().to("schema.ErrorPersonSource").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        PCollectionTuple outputTuple2 = outputTuple.get(ReadPersonSourceShapeFn.successTag).apply("ToPersonTargetShape", ParDo.of(new ToPersonTargetShapeFn()).withOutputTags(ToPersonTargetShapeFn.successTag, TupleTagList.of(ToPersonTargetShapeFn.deadLetterTag)));
        outputTuple2 .get(ToPersonTargetShapeFn.successTag).apply("WritePersonTargetShape", BigQueryIO.writeTableRows().to("schema.PersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputTuple2 .get(ToPersonTargetShapeFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WritePersonTargetErrorLog", BigQueryIO.writeTableRows().to("schema.ErrorPersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        p.run();
    }

    public static void main(String[] args) {
        PersonTargetShapeBeam.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(PersonTargetShapeBeam.Options.class);
        setModifiedUnixTime(options);
        process(options);
    }

    private static void setModifiedUnixTime(PersonTargetShapeBeam.Options options) {
        String stringValue = options.getModifiedDate();
        if (stringValue!= null) {
            options.setModifiedUnixTime((new DateTime(stringValue).getValue()/ 1000));
        }
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

        public Long getModifiedUnixTime();

        public void setModifiedUnixTime(Long modifiedUnixTime);
    }
}
