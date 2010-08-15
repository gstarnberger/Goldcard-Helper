package name.starnberger.guenther.android.goldcard;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class GoldCard extends Activity {
	StringBuffer text = new StringBuffer();
	String rcid = "no CID copied";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        generateRcid();
    }
 
    private void generateRcid() {
       TextView mainText = (TextView) findViewById(R.id.mainText);
        
        text = new StringBuffer();
        // text.append("GoldCard helper\n\n");
               
        File mmcRootDir = new File("/sys/class/mmc_host/");
        
        FilenameFilter mmcFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("mmc");
            }
        };
        
        String[] mmcSubDirs = mmcRootDir.list(mmcFilter);
        
        boolean atLeastOneCard = false;
        
        for(String mmcSubDir: mmcSubDirs) {      
        	File dir = new File(mmcRootDir, mmcSubDir);      	
        
        	String[] children = dir.list(mmcFilter);
                	        	       
        	for(String child: children) {
        		File mmcDir = new File(dir, child);
        		File mmcCid = new File(mmcDir, "cid");
        	
        		if(mmcCid.canRead()) {
        			atLeastOneCard = true;
        			
        			try {
        				FileInputStream fis = new FileInputStream(mmcCid);
        				BufferedInputStream buf = new BufferedInputStream(fis);
        				DataInputStream dis = new DataInputStream(buf);
				
        				String cid = dis.readLine();
        				rcid = reverseGold(cid);
				
        				text.append("Card: " + child + "\n");
        				// text.append("CID: " + cid + "\n");
        				text.append("Reverse CID: " + rcid + "\n\n");
								
        				dis.close();
        				buf.close();
        				fis.close();
        			} catch (FileNotFoundException e) {
        				text.append("ERROR: FileNotFoundException while reading data for MMC card: " + child + "\n\n");
        			} catch (IOException e) {
        				text.append("ERROR: IOException while reading data for MMC card: " + child + "\n\n");
        			}
        		}
        	}
        }
        
    	if(atLeastOneCard == false) {
    		text.append("ERROR: No MMC card found!\n\n");
    	}
        
        text.append("To generate your GoldCard go to http://psas.revskills.de/?q=goldcard and enter the \"reverse\" number in the field \"SD Card Serial (CID)\".\n\n");
        
        text.append("Use \"menu\" to copy the reverse CID to your clipboard or to send it via email.\n\n");
        
        text.append("While this software provides a link to the PSAS goldcard generator, it is not otherwise related to PSAS or the PSAS website.");
        
        mainText.setText(text);
    }
    
	private void showMsg(final String text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	private String reverseGold(String goldString) {
		StringBuffer buffer = new StringBuffer(goldString); 
		buffer = buffer.reverse();
	
		for(int i = 0; i < buffer.length(); i += 2) {
			char tmp = buffer.charAt(i);
			buffer.setCharAt(i, buffer.charAt(i+1));
			buffer.setCharAt(i+1, tmp);
		}
		
		buffer.setCharAt(0, '0');
		buffer.setCharAt(1, '0');
		return buffer.toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.rescan:
			generateRcid();
			showMsg("Rescan finished");
			return true;
		case R.id.clipboard:
			ClipboardManager clipboard = 
			      (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

			 clipboard.setText(rcid);
			 showMsg("Reverse CID copied");
			return true;
		case R.id.share:
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GoldCard generator");
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text.toString());

			startActivity(Intent.createChooser(shareIntent, "Choose destination"));
			return true;
		case R.id.psas:
			Intent viewIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://psas.revskills.de/?q=goldcard"));
			startActivity(viewIntent);
			return true;
		}
		return false;
	}
}