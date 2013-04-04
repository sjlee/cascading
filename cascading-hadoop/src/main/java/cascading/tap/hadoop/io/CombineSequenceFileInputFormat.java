package cascading.tap.hadoop.io;

import java.io.IOException;

import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.CombineFileInputFormat;
import org.apache.hadoop.mapred.lib.CombineFileRecordReader;
import org.apache.hadoop.mapred.lib.CombineFileSplit;

/**
 * Input format that is a CombineFileInputFormat-equivalent for SequentialFileInputFormat.
 *
 * @author sjlee
 *
 */
public class CombineSequenceFileInputFormat<K,V> extends CombineFileInputFormat<K,V>
  {
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public RecordReader<K,V> getRecordReader( InputSplit split, JobConf conf, Reporter reporter ) throws IOException
    {
    return new CombineFileRecordReader( conf, (CombineFileSplit) split, reporter, SequenceFileRecordReaderWrapper.class );
    }
  }
