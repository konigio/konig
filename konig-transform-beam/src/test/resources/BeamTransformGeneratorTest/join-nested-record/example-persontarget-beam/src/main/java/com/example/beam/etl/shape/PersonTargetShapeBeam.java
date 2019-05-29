package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
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
import org.apache.beam.sdk.values.TupleTag;

public class PersonTargetShapeBeam {
    static final TupleTag<TableRow> addressSourceTag = new TupleTag<TableRow>();
    static final TupleTag<TableRow> personNameSourceTag = new TupleTag<TableRow>();

    private static String sourceURI(String pattern, PersonTargetShapeBeam.Options options) {
        return pattern.replace("${environmentName}", options.getEnvironment());
    }

    public static void process(PersonTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        PCollection<FileIO.ReadableFile> addressSource = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/inbound/application/vnd.example.source.address+csv/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadAddressSourceFn()));
        PCollection<FileIO.ReadableFile> personNameSource = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/inbound/application/vnd.example.source.person-name+csv/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonNameSourceFn()));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(addressSourceTag, addressSource).and(personNameSourceTag, personNameSource).apply(CoGroupByKey.<String> create());
        TableRow<TableRow> outputRowCollection = kvpCollection.apply(ParDo.of(new MergeAddressSourceAndPersonNameSourceFn()));
        outputRowCollection.apply("WritePersonTarget", BigQueryIO.writeTableRows().to("schema.PersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
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
