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

public class BqPersonShapeBeam {
    static final TupleTag<TableRow> personAlumniOfTag = new TupleTag<TableRow>();
    static final TupleTag<TableRow> personNameTag = new TupleTag<TableRow>();

    private static String sourceURI(String pattern, BqPersonShapeBeam.Options options) {
        return pattern.replace("${environmentName}", options.getEnvironment());
    }

    public static void process(BqPersonShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        PCollection<FileIO.ReadableFile> personAlumniOf = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonAlumniOfFn()));
        PCollection<FileIO.ReadableFile> personName = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonNameFn()));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(personAlumniOfTag, personAlumniOf).and(personNameTag, personName).apply(CoGroupByKey.<String> create());
        TableRow<TableRow> outputRowCollection = kvpCollection.apply(ParDo.of(new MergePersonAlumniOfAndPersonNameFn()));
        outputRowCollection.apply("WriteBqPerson", BigQueryIO.writeTableRows().to("schema.BqPerson").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
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
    }
}
