package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
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
    static final TupleTag<TableRow> personAlumniOfTag = new TupleTag<TableRow>();
    static final TupleTag<TableRow> personNameTag = new TupleTag<TableRow>();

    private static String sourceURI(String pattern, BqPersonShapeBeam.Options options) {
        return pattern.replace("${environmentName}", options.getEnvironment());
    }

    public static void process(BqPersonShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        TableReference PersonAlumniOfShapeTableRef = new TableReference();
        PersonAlumniOfShapeTableRef.setDatasetId("schema");
        PersonAlumniOfShapeTableRef.setTableId("PersonAlumniOfShape");
        PCollectionTuple personAlumniOf = p.apply(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.readTableRows().from(PersonAlumniOfShapeTableRef)).apply(ParDo.of(new PersonAlumniOfToKvFn()));
        PCollectionTuple personName = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonNameFn()).withOutputTags(ReadPersonNameFn.successTag, TupleTagList.of(ReadPersonNameFn.deadLetterTag)));
        personName.get(ReadPersonNameFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WritePersonNameErrorLog", org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.writeTableRows().to("schema.ErrorPersonName").withCreateDisposition(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(personAlumniOfTag, personAlumniOf).and(personNameTag, personName).apply(CoGroupByKey.<String> create());
        PCollectionTuple outputRowCollection = kvpCollection.apply(ParDo.of(new ToBqPersonShapeFn()).withOutputTags(ToBqPersonShapeFn.successTag, TupleTagList.of(ToBqPersonShapeFn.deadLetterTag)));
        outputRowCollection.get(ToBqPersonShapeFn.successTag).apply("WriteBqPerson", org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.writeTableRows().to("schema.BqPerson").withCreateDisposition(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputRowCollection.get(ToBqPersonShapeFn.deadLetterTag).setCoder(TableRowJsonCoder.of()).apply("WriteBqPersonErrorLog", org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.writeTableRows().to("schema.ErrorBqPerson").withCreateDisposition(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        p.run();
    }

    public static void main(String[] args) {
        BqPersonShapeBeam.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(BqPersonShapeBeam.Options.class);
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
    }
}
