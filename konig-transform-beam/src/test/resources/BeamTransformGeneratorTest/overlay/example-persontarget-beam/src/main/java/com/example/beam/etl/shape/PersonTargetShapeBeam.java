package com.example.beam.etl.shape;

import java.text.MessageFormat;
import java.util.UUID;
import com.google.api.services.bigquery.model.TableReference;
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

public class PersonTargetShapeBeam {
    static final TupleTag<TableRow> personSourceTag = new TupleTag<TableRow>();
    static final TupleTag<TableRow> personTargetTag = new TupleTag<TableRow>();

    private static String sourceURI(String pattern, PersonTargetShapeBeam.Options options) {
        return pattern.replace("${environmentName}", options.getEnvironment());
    }

    public static void process(PersonTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        String pattern = "gs://example-inbound-${environmentName}/invalid/{0}".replace("${environmentName}", options.getEnvironment());
        PCollectionTuple personSource = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonSourceFn()).withOutputTags(ReadPersonSourceFn.successTag, TupleTagList.of(ReadPersonSourceFn.deadLetterTag)));
        personSource.get(ReadPersonSourceFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.ns.shape.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".csv")));
        TableReference targetTableRef = new TableReference();
        targetTableRef.setDatasetId("schema");
        targetTableRef.setTableId("PersonTarget");
        PCollection<KV<String, TableRow>> personTargetTable = p.apply(BigQueryIO.readTableRows().from(targetTableRef)).apply(ParDo.of(new PersonTargetToKvFn()));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(personSourceTag, personSource.get(ReadPersonSourceFn.successTag)).and(personTargetTag, personTargetTable.get(PersonTargetToKvFn.successTag)).apply(CoGroupByKey.<String> create());
        PCollectionTuple outputRowCollection = kvpCollection.apply(ParDo.of(new PersonTargetMergeFn()).withOutputTags(PersonTargetMergeFn.successTag, TupleTagList.of(PersonTargetMergeFn.deadLetterTag)));
        outputRowCollection.get(PersonTargetMergeFn.successTag).apply("WritePersonTarget", BigQueryIO.writeTableRows().to("schema.PersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputRowCollection.get(PersonTargetMergeFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.ns.shape.person")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".txt")));
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
