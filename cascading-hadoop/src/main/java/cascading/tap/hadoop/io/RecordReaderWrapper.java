package cascading.tap.hadoop.io;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.CombineFileSplit;

/**
 * A wrapper class for a record reader that handles a single file split. It delegates most of the
 * methods to the wrapped instance. We need this wrapper to satisfy the constructor requirement to
 * be used with hadoop's CombineFileRecordReader class.
 *
 * @author sjlee
 *
 * @see org.apache.hadoop.mapred.lib.CombineFileRecordReader
 * @see org.apache.hadoop.mapred.lib.CombineFileInputFormat
 */
class RecordReaderWrapper<K,V> implements RecordReader<K,V>
  {
  private final RecordReader<K,V> delegate;

  protected RecordReaderWrapper( FileInputFormat<K,V> inputFormat, CombineFileSplit split, Configuration conf, Reporter reporter, Integer idx ) throws IOException
    {
    FileSplit fileSplit = new FileSplit(split.getPath(idx),
        split.getOffset(idx),
        split.getLength(idx),
        split.getLocations());

    delegate = inputFormat.getRecordReader( fileSplit, (JobConf) conf, reporter );
    }

  public boolean next( K key, V value ) throws IOException
    {
    return delegate.next( key, value );
    }

  public K createKey()
    {
    return delegate.createKey();
    }

  public V createValue()
    {
    return delegate.createValue();
    }

  public long getPos() throws IOException
    {
    return delegate.getPos();
    }

  public void close() throws IOException
    {
    delegate.close();
    }

  public float getProgress() throws IOException
    {
    return delegate.getProgress();
    }
  }