/* This Class represents a Reconstruct Series. */

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;

import java.io.*;

import java.awt.image.*;
import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.awt.image.*;
import javax.imageio.ImageIO;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class SeriesClass {

  HashMap<String, String> attributes = new HashMap<String, String>();
  String series_path = null;
  Document series_doc = null;
  Document section_docs[] = null;

  SectionClass sections[] = null;
  int section_index = 0;

  public SeriesClass ( File series_file ) {

    this.series_path = series_file.getParent();

    String series_file_name = series_file.getName();
    this.series_doc = XML_Parser.parse_xml_file_to_doc ( series_file );

    String section_file_names[] = get_section_file_names ( series_path, series_file_name.substring(0,series_file_name.length()-4) );


    Element series_element = this.series_doc.getDocumentElement();
    System.out.println ( "Series is currently viewing index: " + series_element.getAttribute("index") );

    this.section_docs = new Document[section_file_names.length];

    sections = new SectionClass[section_file_names.length];


    for (int i=0; i<section_file_names.length; i++) {
      File section_file;

      section_file = new File ( series_path + File.separator + section_file_names[i] );

      this.section_docs[i] = XML_Parser.parse_xml_file_to_doc ( section_file );

      sections[i] = new SectionClass ( series_path, section_file_names[i] );

      Element section_element = this.section_docs[i].getDocumentElement();

      // System.out.println ( "This section is index " + this.section_docs[i].getDocumentElement().getAttributes().getNamedItem("index").getNodeValue() );
    }

    section_index = 0;
  }

  public void dump() {
    System.out.println ( "============== Begin Series File  ================" );
    XML_Parser.dump_doc(this.series_doc);
    // XML_Parser.dump_doc(this.series_doc);
    System.out.println ( "============== End Series File  ================" );
    if (this.section_docs != null) {
      for (int i=0; i<this.section_docs.length; i++) {
        System.out.println ( "============== Begin Section File  ================" );
        XML_Parser.dump_doc(this.section_docs[i]);
        System.out.println ( "============== End Section File  ================" );
      }
    }
  }

  public void dump_strokes() {
	  if (sections != null) {
	    if (section_index < sections.length) {
	      sections[section_index].dump_strokes();
	    }
    }
  }

  public void clear_strokes() {
	  if (sections != null) {
	    if (section_index < sections.length) {
	      sections[section_index].clear_strokes();
	    }
    }
  }

  public void add_stroke (	ArrayList<double[]> stroke ) {
	  if (sections != null) {
	    if (section_index < sections.length) {
	      sections[section_index].add_stroke ( stroke );
	    }
	  }
  }

  public void position_by_n_sections ( int n ) {
    section_index += n;
    if (section_index < 0) {
      section_index = 0;
    } else if (section_index >= sections.length) {
      section_index = sections.length - 1;
    }
  }

	public void paint_section (Graphics g, Reconstruct r) {
	  // BufferedImage image_frame = null;
	  if (sections != null) {
	    if (section_index < sections.length) {
	      sections[section_index].paint_section ( g, r, this );
	    }
	  }
	}


  public String[] get_section_file_names ( String path, String root_name ) {
    // System.out.println ( "Looking for " + root_name + " in " + path );
    File all_files[] = new File (path).listFiles();

    if (all_files==null) {
      return ( new String[0] );
    }
    if (all_files.length <= 0) {
      return ( new String[0] );
    }
    String matched_files[] = new String[all_files.length];
    int num_matched = 0;
    for (int i=0; i<all_files.length; i++) {
      matched_files[i] = null;
      String fn = all_files[i].getName();
      if ( fn.startsWith(root_name+".") ) {
        // This is a file of the form root_name.[something]
        // Verify that the "something" matches properly
        if ( fn.matches(root_name+"\\.[0123456789]+") ) {
          matched_files[i] = fn;
          // System.out.println ( "Found match: " + matched_files[i] );
          num_matched += 1;
        }
      }
    }
    String matched_names[] = new String[num_matched];
    int next_match_index = 0;
    for (int i=0; i<matched_files.length; i++) {
      if (matched_files[i] != null) {
        matched_names[next_match_index] = matched_files[i];
        next_match_index += 1;
      }
    }
    
    if (matched_names.length > 1) {
      // Sort the names so the sections will be in order (easier now than later)
      
      // Unfortunately, Arrays.sort will be sort as strings to give 1, 10, 11, 12 ... 19, 2, 20, 21 ... 29, 3, 30
      // So pad each with zeros until they are all the same length.
      
      // Get the maximum length
      int max_length = 0;
      for (int i=0; i<matched_names.length; i++) {
        int this_length = matched_names[i].length();
        if (this_length > max_length) {
          max_length = this_length;
        }
      }
      
      // Pad each to be the maximum length
      for (int i=0; i<matched_names.length; i++) {
        String s = matched_names[i];
        int this_length = s.length();
        if (this_length < max_length) {
          // Pad with zeros
          String zeros = "";
          for (int j=0; j<(max_length-this_length); j++) {
            zeros = zeros + "0";
          }
          int lastdot = s.lastIndexOf('.');
          matched_names[i] = s.substring(0,lastdot+1) + zeros + s.substring(lastdot+1);
        }
      }

      // Then sort them as strings (leading zeros will make them all comparable)
      Arrays.sort ( matched_names );
      
      // Finally, remove the leading zeros
      for (int i=0; i<matched_names.length; i++) {
        String s = matched_names[i];
        int lastdot = s.lastIndexOf('.');
        while ( s.charAt(lastdot+1) == '0' ) {
          s = s.substring(0,lastdot+1) + s.substring(lastdot+2);
        }
        matched_names[i] = s;
      }

    }

    // Print them to verify during testing
    //for (int i=0; i<matched_names.length; i++) {
    //  System.out.println ( "Sorted name: " + matched_names[i] );
    //}

    return ( matched_names );
  }


	public static void main ( String[] args ) {
		System.out.println ( "Testing SeriesClass.java ..." );
	}

}
