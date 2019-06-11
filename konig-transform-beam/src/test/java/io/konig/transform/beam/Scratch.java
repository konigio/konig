package io.konig.transform.beam;

import org.apache.avro.data.ErrorBuilder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;

import com.google.api.services.bigquery.model.TableRow;

public class Scratch {
	class ToPersonTargetShapeFn
  extends DoFn<TableRow, TableRow>
{

  @DoFn.ProcessElement
  public void processElement(ProcessContext c) {
      try {
          ErrorBuilder errorBuilder = new ErrorBuilder();
          TableRow outputRow = new TableRow();
          TableRow personSourceRow = c.element();
          externalIdentifier(personSourceRow, outputRow, errorBuilder);
          givenName(personSourceRow, outputRow, errorBuilder);
          if (!outputRow.isEmpty()) {
              c.output(outputRow);
          }
      } catch (final Throwable oops) {
          oops.printStackTrace();
      }
  }

  private boolean externalIdentifier(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      boolean ok = true;
      if (personSourceRow.get("crm_id")!= null) {
          ok = (ok&&externalIdentifier__crm_id(personSourceRow, outputRow, errorBuilder));
      }
      if (personSourceRow.get("mdm_id")!= null) {
          ok = (ok&&externalIdentifier__mdm_id(personSourceRow, outputRow, errorBuilder));
      }
      return ok;
  }

  private boolean externalIdentifier__crm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      boolean ok = true;
      TableRow externalIdentifier = new TableRow();
      externalIdentifier_identifiedByValue__crm_id(personSourceRow, externalIdentifier, errorBuilder);
      externalIdentifier_originatingFeed__crm_id(personSourceRow, externalIdentifier, errorBuilder);
      if (errorBuilder.isEmpty()&&(!externalIdentifier.isEmpty())) {
          outputRow.set("externalIdentifier", externalIdentifier);
          return true;
      } else {
          return false;
      }
  }

  private boolean externalIdentifier_identifiedByValue__crm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      TableRow identifiedByValue = new TableRow();
      externalIdentifier_identifiedByValue_identifier__crm_id(personSourceRow, outputRow, errorBuilder);
      externalIdentifier_identifiedByValue_identityProvider__crm_id(personSourceRow, outputRow, errorBuilder);
      if (errorBuilder.isEmpty()&&(!identifiedByValue.isEmpty())) {
          outputRow.set("identifiedByValue", identifiedByValue);
          return true;
      } else {
          return false;
      }
  }

  private boolean externalIdentifier_identifiedByValue_identifier__crm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object crm_id = ((personSourceRow == null)?null:personSourceRow.get("crm_id"));
      if (crm_id!= null) {
          outputRow.set("identifier", crm_id);
          return true;
      } else {
          errorBuilder.addError("Cannot set externalIdentifier.identifiedByValue.identifier because {PersonSourceShape}.crm_id is null");
          return false;
      }
  }

  private boolean externalIdentifier_identifiedByValue_identityProvider__crm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object originating_system = ((personSourceRow == null)?null:personSourceRow.get("originating_system"));
      if (originating_system!= null) {
          outputRow.set("identityProvider", localName(((String) concat("http://example.com/ns/sys/", stripSpaces(originating_system.toString()), ".CRM"))));
          return true;
      } else {
          errorBuilder.addError("Cannot set externalIdentifier.identifiedByValue.identityProvider because {PersonSourceShape}.externalIdentifier.originating_system is null");
          return false;
      }
  }

  private String concat(Object... arg) {
      for (Object obj: arg) {
          if (obj == null) {
              return null;
          }
      }
      StringBuilder builder = new StringBuilder();
      for (Object obj: arg) {
          builder.append(obj);
      }
      return builder.toString();
  }

  private String stripSpaces(String text) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; (i<text.length()); ) {
          int c = text.codePointAt(i);
          if (Character.isSpaceChar(c)) {
              builder.appendCodePoint(c);
          }
          i += Character.charCount(c);
      }
      return builder.toString();
  }

  private String localName(String iriString) {
      if (iriString!= null) {
          int start = iriString.lastIndexOf('/');
          if (start< 0) {
              start = iriString.lastIndexOf('#');
              if (start< 0) {
                  start = iriString.lastIndexOf(':');
              }
          }
          if (start >= 0) {
              return iriString.substring((start + 1));
          }
      }
      return null;
  }

  private boolean externalIdentifier_originatingFeed__crm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object originating_feed = ((personSourceRow == null)?null:personSourceRow.get("originating_feed"));
      if (originating_feed!= null) {
          outputRow.set("originatingFeed", originating_feed);
          return true;
      } else {
          errorBuilder.addError("Cannot set externalIdentifier.originatingFeed because {PersonSourceShape}.originating_feed is null");
          return false;
      }
  }

  private boolean externalIdentifier__mdm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      boolean ok = true;
      TableRow externalIdentifier = new TableRow();
      externalIdentifier_identifiedByValue__mdm_id(personSourceRow, externalIdentifier, errorBuilder);
      externalIdentifier_originatingFeed__mdm_id(personSourceRow, externalIdentifier, errorBuilder);
      if (errorBuilder.isEmpty()&&(!externalIdentifier.isEmpty())) {
          outputRow.set("externalIdentifier", externalIdentifier);
          return true;
      } else {
          return false;
      }
  }

  private boolean externalIdentifier_identifiedByValue__mdm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      TableRow identifiedByValue = new TableRow();
      externalIdentifier_identifiedByValue_identifier__mdm_id(personSourceRow, outputRow, errorBuilder);
      externalIdentifier_identifiedByValue_identityProvider__mdm_id(personSourceRow, outputRow, errorBuilder);
      if (errorBuilder.isEmpty()&&(!identifiedByValue.isEmpty())) {
          outputRow.set("identifiedByValue", identifiedByValue);
          return true;
      } else {
          return false;
      }
  }

  private boolean externalIdentifier_identifiedByValue_identifier__mdm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object mdm_id = ((personSourceRow == null)?null:personSourceRow.get("mdm_id"));
      if (mdm_id!= null) {
          outputRow.set("identifier", mdm_id);
          return true;
      } else {
          errorBuilder.addError("Cannot set externalIdentifier.identifiedByValue.identifier because {PersonSourceShape}.mdm_id is null");
          return false;
      }
  }

  private boolean externalIdentifier_identifiedByValue_identityProvider__mdm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object originating_system = ((personSourceRow == null)?null:personSourceRow.get("originating_system"));
      if (originating_system!= null) {
          outputRow.set("identityProvider", localName(((String) concat("http://example.com/ns/sys/", stripSpaces(originating_system.toString()), ".MDM"))));
          return true;
      } else {
          errorBuilder.addError("Cannot set externalIdentifier.identifiedByValue.identityProvider because {PersonSourceShape}.externalIdentifier.originating_system is null");
          return false;
      }
  }

  private boolean externalIdentifier_originatingFeed__mdm_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object originating_feed = ((personSourceRow == null)?null:personSourceRow.get("originating_feed"));
      if (originating_feed!= null) {
          outputRow.set("originatingFeed", originating_feed);
          return true;
      } else {
          errorBuilder.addError("Cannot set externalIdentifier.originatingFeed because {PersonSourceShape}.originating_feed is null");
          return false;
      }
  }

  private boolean givenName(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
      Object first_name = ((personSourceRow == null)?null:personSourceRow.get("first_name"));
      if (first_name!= null) {
          outputRow.set("givenName", first_name);
          return true;
      } else {
          errorBuilder.addError("Cannot set givenName because {PersonSourceShape}.first_name is null");
          return false;
      }
  }
}


	class ErrorBuilder {
    private StringBuilder buffer;

    public boolean isEmpty() {
        return (buffer.length() == 0);
    }

    public void addError(String text) {
        if (!isEmpty()) {
            buffer.append("; ");
        }
        buffer.append(text);
    }

    public String toString() {
        return buffer.toString();
    }
}
}
