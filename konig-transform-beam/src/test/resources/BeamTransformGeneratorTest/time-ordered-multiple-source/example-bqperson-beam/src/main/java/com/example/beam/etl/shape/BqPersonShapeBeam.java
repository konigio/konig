package com.example.beam.etl.shape;

import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.TableRowJsonCoder;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;

public class BqPersonShapeBeam {
    static final TupleTag<TableRow> personContactTag = new TupleTag<TableRow>();
    static final TupleTag<TableRow> personNameTag = new TupleTag<TableRow>();

    private static String sourceURI(String pattern, BqPersonShapeBeam.Options options) {
        return pattern.replace("${environmentName}", options.getEnvironment());
    }

    public static void process(BqPersonShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        PCollectionTuple personContact = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/inbound/application/vnd.pearson.personcontact+csv/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonContactFn()).withOutputTags(ReadPersonContactFn.successTag, TupleTagList.of(ReadPersonContactFn.deadLetterTag)));
        personContact.get(ReadPersonContactFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WritePersonContactErrorLog", BigQueryIO.writeTableRows().to("schema.ErrorPersonContact").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        PCollectionTuple personName = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/inbound/application/vnd.pearson.person+csv/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonNameFn()).withOutputTags(ReadPersonNameFn.successTag, TupleTagList.of(ReadPersonNameFn.deadLetterTag)));
        personName.get(ReadPersonNameFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WritePersonNameErrorLog", BigQueryIO.writeTableRows().to("schema.ErrorPersonName").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(personContactTag, personContact.get(ReadPersonContactFn.successTag)).and(personNameTag, personName.get(ReadPersonNameFn.successTag)).apply(CoGroupByKey.<String> create());
        PCollectionTuple outputRowCollection = kvpCollection.apply(ParDo.of(new ToBqPersonShapeFn()).withOutputTags(ToBqPersonShapeFn.successTag, TupleTagList.of(ToBqPersonShapeFn.deadLetterTag)));
        outputRowCollection.get(ToBqPersonShapeFn.successTag).apply("WriteBqPerson", BigQueryIO.writeTableRows().to("schema.BqPerson").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputRowCollection.get(ToBqPersonShapeFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WriteBqPersonErrorLog", BigQueryIO.writeTableRows().to("schema.ErrorBqPerson").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        p.run();
    }

    public static void main(String[] args) {
        BqPersonShapeBeam.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(BqPersonShapeBeam.Options.class);
        setBatchBeginUnixTime(options);
        setModifiedUnixTime(options);
        process(options);
    }

    private static void setBatchBeginUnixTime(BqPersonShapeBeam.Options options) {
        options.setBatchBeginUnixTime((new DateTime(options.getBatchBegin()).getValue()/ 1000));
    }

    private static void setModifiedUnixTime(BqPersonShapeBeam.Options options) {
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

        @Validation.Required
        @Description("The date/time for the beginning of the batch window in ISO 8601 format")
        public String getBatchBegin();

        public void setBatchBegin(String batchBegin);

        public Long getBatchBeginUnixTime();

        public void setBatchBeginUnixTime(Long batchBeginUnixTime);

        public Long getModifiedUnixTime();

        public void setModifiedUnixTime(Long modifiedUnixTime);
    }
}
