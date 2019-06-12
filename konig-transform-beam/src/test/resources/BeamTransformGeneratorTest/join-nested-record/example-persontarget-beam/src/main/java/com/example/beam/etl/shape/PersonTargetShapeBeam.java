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

public class PersonTargetShapeBeam {
    static final TupleTag<TableRow> addressSourceTag = new TupleTag<TableRow>();
    static final TupleTag<TableRow> personNameSourceTag = new TupleTag<TableRow>();

    private static String sourceURI(String pattern, PersonTargetShapeBeam.Options options) {
        return pattern.replace("${environmentName}", options.getEnvironment());
    }

    public static void process(PersonTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        String pattern = "gs://example-inbound-${environmentName}/invalid/{0}".replace("${environmentName}", options.getEnvironment());
        PCollectionTuple addressSource = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/inbound/application/vnd.example.source.address+csv/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadAddressSourceFn()).withOutputTags(ReadAddressSourceFn.successTag, TupleTagList.of(ReadAddressSourceFn.deadLetterTag)));
        addressSource.get(ReadAddressSourceFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.source.address")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".csv")));
        PCollectionTuple personNameSource = p.apply(FileIO.match().filepattern(sourceURI("gs://example-inbound-${environmentName}/inbound/application/vnd.example.source.person-name+csv/*", options))).apply(FileIO.readMatches()).apply(ParDo.of(new ReadPersonNameSourceFn()).withOutputTags(ReadPersonNameSourceFn.successTag, TupleTagList.of(ReadPersonNameSourceFn.deadLetterTag)));
        personNameSource.get(ReadPersonNameSourceFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

            @Override
            public Object apply(Object input) {
                return UUID.randomUUID().toString();
            }
        }
        ).to(MessageFormat.format(pattern, "application/vnd.example.source.person-name")).withNumShards(1).withDestinationCoder(StringUtf8Coder.of()).withNaming(key -> FileIO.Write.defaultNaming(key, ".csv")));
        PCollection<KV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple.of(addressSourceTag, addressSource.get(ReadAddressSourceFn.successTag)).and(personNameSourceTag, personNameSource.get(ReadPersonNameSourceFn.successTag)).apply(CoGroupByKey.<String> create());
        PCollectionTuple outputRowCollection = kvpCollection.apply(ParDo.of(new MergeAddressSourceAndPersonNameSourceFn()).withOutputTags(MergeAddressSourceAndPersonNameSourceFn.successTag, TupleTagList.of(MergeAddressSourceAndPersonNameSourceFn.deadLetterTag)));
        outputRowCollection.get(MergeAddressSourceAndPersonNameSourceFn.successTag).apply("WritePersonTarget", BigQueryIO.writeTableRows().to("schema.PersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        outputRowCollection.get(MergeAddressSourceAndPersonNameSourceFn.deadLetterTag).setCoder(StringUtf8Coder.of()).apply("writeErrorDocument", FileIO.<String, String> writeDynamic().via(TextIO.sink()).by(new SerializableFunction() {

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
