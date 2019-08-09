package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
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

    public static void process(BqPersonShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        TableReference PersonContactShapeTableRef = new TableReference();
        PersonContactShapeTableRef.setDatasetId("schema");
        PersonContactShapeTableRef.setTableId("PersonContactShape");
        PCollectionTuple personContact = p.apply(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.readTableRows().from(PersonContactShapeTableRef)).apply(ParDo.of(new PersonContactToKvFn()).withOutputTags(PersonContactToKvFn.successTag, TupleTagList.of(PersonContactToKvFn.deadLetterTag)));
        TableReference PersonNameShapeTableRef = new TableReference();
        PersonNameShapeTableRef.setDatasetId("schema");
        PersonNameShapeTableRef.setTableId("PersonNameShape");
        PCollectionTuple personName = p.apply(org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.readTableRows().from(PersonNameShapeTableRef)).apply(ParDo.of(new PersonNameToKvFn()).withOutputTags(PersonNameToKvFn.successTag, TupleTagList.of(PersonNameToKvFn.deadLetterTag)));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(personContactTag, personContact.get(PersonContactToKvFn.successTag)).and(personNameTag, personName.get(PersonNameToKvFn.successTag)).apply(CoGroupByKey.<String> create());
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
