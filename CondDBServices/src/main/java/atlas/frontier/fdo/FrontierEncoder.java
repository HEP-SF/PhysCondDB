/**
 * Frontier encoder interface
 * $Id: Encoder.java,v 1.3 2010/07/28 22:05:30 dwd Exp $
 *
 * @author: Sergey Kosyakov
 *
 * Copyright (c) 2009, FERMI NATIONAL ACCELERATOR LABORATORY
 * All rights reserved. 
 *
 * For details of the Fermitools (BSD) license see Fermilab-2009.txt or
 *  http://fermitools.fnal.gov/about/terms.html
 */

package atlas.frontier.fdo;

import java.io.InputStream;

public interface FrontierEncoder
 {
  public void writeEOR() throws Exception;  // Writes End Of Record marker
  public void flush() throws Exception;
  public void writeInt(int v) throws Exception;
  public void writeLong(long v) throws Exception;
  public void writeDouble(double v) throws Exception;
  public void writeFloat(float v) throws Exception;
  public void writeString(String v) throws Exception;
  public void writeBytes(byte[] v) throws Exception;
  public void writeStream(InputStream is,int len) throws Exception;
  public void writeDate(java.util.Date v) throws Exception;
  public long getOutputSize();
  public byte[] getMD5Digest() throws Exception;
  public void close() throws Exception;
 }

