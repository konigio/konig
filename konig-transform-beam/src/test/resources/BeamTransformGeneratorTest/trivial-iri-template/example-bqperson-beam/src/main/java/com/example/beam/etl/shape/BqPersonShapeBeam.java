package com.example.beam.etl.shape;

import java.text.MessageFormat;
import java.util.UUID;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
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
        String pattern = "gs://example-inbound-${environmentName}/invalid/{0}".replace("${environmentName}", options.getEnvironment());
        PCollectionTuple personAlumniOf = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonAlumniOfFn()).withOutputTags(ReadPersonAlumniOfFn.successTag, TupleTagList.of(ReadPersonAlumniOfFn.deadLetterTag)));
        personAlumniOf.get(ReadPersonAlumniOfFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.example.com.shapes.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".csv")));
        PCollectionTuple personName = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonNameFn()).withOutputTags(ReadPersonNameFn.successTag, TupleTagList.of(ReadPersonNameFn.deadLetterTag)));
        personName.get(ReadPersonNameFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.example.com.shapes.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".csv")));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(personAlumniOfTag, personAlumniOf.get(ReadPersonAlumniOfFn.successTag)).and(personNameTag, personName.get(ReadPersonNameFn.successTag)).apply(CoGroupByKey.<String> create());
        PCollectionTuple outputRowCollection = kvpCollection.apply(ParDo.of(new MergePersonAlumniOfAndPersonNameFn()).withOutputTags(MergePersonAlumniOfAndPersonNameFn.successTag, TupleTagList.of(MergePersonAlumniOfAndPersonNameFn.deadLetterTag)));
        outputRowCollection.get(MergePersonAlumniOfAndPersonNameFn.successTag).apply("WriteBqPerson", BigQueryIO.writeTableRows().to("schema.BqPerson").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputRowCollection.get(MergePersonAlumniOfAndPersonNameFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.example.com.shapes.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".txt")));
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
    }
}
