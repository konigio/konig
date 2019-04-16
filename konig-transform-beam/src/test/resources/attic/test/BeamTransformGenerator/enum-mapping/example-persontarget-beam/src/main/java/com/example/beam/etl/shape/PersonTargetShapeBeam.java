package com.example.beam.etl.shape;

import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.ParDo;

public class PersonTargetShapeBeam {

    private static String sourceURI(PersonTargetShapeBeam.Options options) {
        String envName = options.getEnvironment();
        return ("gs://personsourceshape-"+ envName);
    }

    public static void process(PersonTargetShapeBeam.Options options) {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
        String sourceURI = sourceURI(options);
        p.apply(FileIO.match().filepattern(sourceURI))
        	.apply(FileIO.readMatches())
        	.apply("ReadFiles", ParDo.of(new ReadPersonSourceShapeFn()))
        	.apply("ToPersonTargetShape", ParDo.of(new ToPersonTargetShapeFn()))
        	.apply("WritePersonTargetShape", BigQueryIO.writeTableRows().to("schema.PersonTarget").withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER).withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
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
