package cascading.tap.hadoop.io;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.lib.CombineFileSplit;

class SequenceFileRecordReaderWrapper<K,V> extends RecordReaderWrapper<K,V>
  {
  // this constructor signature is required by CombineFileRecordReader
  public SequenceFileRecordReaderWrapper( CombineFileSplit split, Configuration conf, Reporter reporter, Integer idx ) throws IOException
    {
    super( new SequenceFileInputFormat<K,V>(), split, conf, reporter, idx );
    }
  }
