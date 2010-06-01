package org.sonatype.nexus.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * Holds basic information about Indexer field, how it is stored. To keep this centralized, and not spread across code.
 * Since Lucene 2.x, the field names are encoded, so please use real chatty names instead of cryptic chars!
 * 
 * @author cstamas
 */
public class IndexerField
{
    private final org.sonatype.nexus.index.Field ontology;

    private final IndexerFieldVersion version;

    private final String key;

    private final Store storeMethod;

    private final Index indexMethod;

    private final TermVector termVector;

    public IndexerField( final org.sonatype.nexus.index.Field ontology, final IndexerFieldVersion version,
                         final String key, final String description, final Store storeMethod, final Index indexMethod )
    {
        this( ontology, version, key, description, storeMethod, indexMethod, null );
    }

    public IndexerField( final org.sonatype.nexus.index.Field ontology, final IndexerFieldVersion version,
                         final String key, final String description, final Store storeMethod, final Index indexMethod,
                         final TermVector termVector )
    {
        this.ontology = ontology;

        this.version = version;

        this.key = key;

        this.storeMethod = storeMethod;

        this.indexMethod = indexMethod;

        this.termVector = termVector;

        ontology.addIndexerField( this );
    }

    public org.sonatype.nexus.index.Field getOntology()
    {
        return ontology;
    }

    public IndexerFieldVersion getVersion()
    {
        return version;
    }

    public String getKey()
    {
        return key;
    }

    public Field.Store getStoreMethod()
    {
        return storeMethod;
    }

    public Field.Index getIndexMethod()
    {
        return indexMethod;
    }

    public Field.TermVector getTermVector()
    {
        return termVector;
    }

    public boolean isIndexed()
    {
        return !Index.NO.equals( indexMethod );
    }

    public boolean isKeyword()
    {
        return isIndexed() && !Index.TOKENIZED.equals( indexMethod );
    }

    public boolean isStored()
    {
        return !( Store.NO.equals( storeMethod ) );
    }

    public void toFieldValue( Document d, ArtifactInfoRecord rec, FieldValue<?> fv )
    {
        if ( isStored() )
        {
            String rawValue = d.get( getKey() );

            fv.setRawValue( rawValue );
            
            rec.addFieldValue( fv );
        }
    }

    public void toLuceneField( ArtifactInfoRecord rec, Document doc )
    {
        if ( !isIndexed() && !isStored() )
        {
            return;
        }

        FieldValue<?> fieldValue = rec.get( getOntology() );

        if ( fieldValue != null )
        {
            String rawValue = fieldValue.getRawValue();

            if ( rawValue != null )
            {
                Field field = null;

                if ( getTermVector() != null )
                {
                    field = new Field( getKey(), rawValue, getStoreMethod(), getIndexMethod(), getTermVector() );
                }
                else
                {
                    field = new Field( getKey(), rawValue, getStoreMethod(), getIndexMethod() );
                }

                doc.add( field );
            }
        }
    }
}
