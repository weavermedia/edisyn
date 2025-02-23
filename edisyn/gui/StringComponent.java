/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import edisyn.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;


/**
   A simple widget maintains a string value in the model.  This widget appears
   as a BUTTON with the string value on it.  When the button is pressed, the user
   is prompted to change the text value.  We might change this to a text field
   in the future, but this at least makes it easy for us to guarantee constraints
   on the text.

   @author Sean Luke
*/

public class StringComponent extends JComponent implements Updatable, HasKey
    {
    JLabel label;
    JButton change;
    Random rand = new Random();
    int maxLength;
        
    String key;
    String instructions;
    Synth synth;
    
    
    static String[] wordList = null;
    
    public String getKey() { return key; }

    /** Returns the current value for the key in the model. */
    public String getText() { return synth.getModel().get(key, ""); }
        
    /** Sets the current value for the key in the model. */
    public void setText(String val) 
        { 
        String state = getText(); 
        if (state == null || !val.equals(state)) 
            {
            synth.getModel().set(key, val); 
            update(key, synth.getModel());
            repaint();
            }
        }
        
    public void update(String key, Model model)
        {
        change.setText(getText().trim());
        }
        
    /** Override this method to verify whether the string is a valid one to store. */
    public boolean isValid(String val)
        {
        return true;
        }
      
    /** Override this method to replace the string with a valid one.  The default
        returns null, which instructs StringComponent to instead call isValid() and
        report to the user that the name is not valid. */
    public String replace(String val)
        {
        return null;
        }
      
    /** Override this method to convert val prior to determining if it is valid, perhaps
        to make it all-uppercase, for example. */  
    public String convert(String val)
        {
        return val;
        }
    
    public JButton getButton() { return change; }
    public String getTitle() { return label.getText().trim(); }
    public String getCommand() { return "Enter " + getTitle(); }
    
    public StringComponent(final String _label, final Synth synth, final String key, int maxLength, String instructions)
        {
        super();
        this.key = key;
        this.synth = synth;
        this.maxLength = maxLength;
        this.instructions = instructions;
        synth.getModel().register(key, this);
                
        setBackground(Style.BACKGROUND_COLOR());
        setLayout(new BorderLayout());
                
        label = new JLabel("  " + _label);
        if (_label != null)
            {
            label.setFont(Style.SMALL_FONT());
            label.setBackground(Style.BACKGROUND_COLOR());  // Style.TRANSPARENT);
            label.setForeground(Style.TEXT_COLOR());
            add(label, BorderLayout.NORTH);
            }
 
        String txt = "";
        for(int i = 0; i < maxLength; i++)
            txt = txt + "m";
        change = new JButton(txt);
        change.putClientProperty("JComponent.sizeVariant", "small");
        change.setFont(Style.SMALL_FONT());
        change.setPreferredSize(change.getPreferredSize());
        change.setHorizontalAlignment(SwingConstants.CENTER);
                
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                perform(StringComponent.this);
                }
            });
                
        add(change, BorderLayout.SOUTH);
        synth.getModel().set(key, "");          // gotta set it to something
        }
        
    public void perform(Component parent)
        {
        String currentText = synth.getModel().get(key, "");
        while(true)
            {
            String val = synth.getModel().get(key, "").trim();
            VBox vbox = new VBox();
            vbox.add(new JLabel(getCommand()));
            final JTextField text = new JTextField(maxLength);
            text.setText(currentText);
                    
            // The following hack is inspired by https://tips4java.wordpress.com/2010/03/14/dialog-focus/
            // and results in the text field being selected (which is what should have happened in the first place) 
                    
            text.addAncestorListener(new javax.swing.event.AncestorListener()
                {
                public void ancestorAdded(javax.swing.event.AncestorEvent e)    
                    { 
                    JComponent component = e.getComponent();
                    component.requestFocusInWindow();
                    text.selectAll(); 
                    }
                public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
                public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
                });
            vbox.add(text);
                    
            synth.disableMenuBar();
            int opt = JOptionPane.showOptionDialog(parent, vbox, getTitle(),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Enter", "Cancel", "Rand", "Rules" }, "Enter");
            synth.enableMenuBar();
                                        
            if (opt == 3)       // this is "Rules"
                {
                synth.disableMenuBar();
                JOptionPane.showMessageDialog(parent, instructions, "Rules", JOptionPane.INFORMATION_MESSAGE);
                synth.enableMenuBar();
                }
            else if (opt == 2)       // this is "Rand"
                {
                if (wordList == null)
                    {
                    Scanner scan = new Scanner(StringComponent.class.getResourceAsStream("wordlist.txt"));
                    ArrayList<String> tokens = new ArrayList<>();
                    while (scan.hasNext()) tokens.add(scan.nextLine());
                    wordList = tokens.toArray(new String[0]);
                    }
                if (wordList != null && wordList.length > 0)
                    {
                    String word = wordList[rand.nextInt(wordList.length)];
                    currentText = word.substring(0, 1).toUpperCase() + word.substring(1);
                    //currentText = wordList[rand.nextInt(wordList.length)].toUpperCase();
                    }
                else
                    {
                    synth.showSimpleError("Random Word", "Error in loading random wordList, sorry.");
                    }
                }
            else if (opt == 1)  // this is "Cancel"
                { 
                return; 
                }
            else                                // This is "Enter"
                {
                String result = convert(text.getText());
                if (result == null) return;
                String str = replace(result);
                if (str == null)
                    {
                    if (isValid(result))
                        { 
                        setText(result); 
                        return;
                        }
                    }
                else
                    {
                    setText(str); 
                    return;
                    }
                }
            }
        }

    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;

        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR());
        graphics.fill(rect);
        }
    }
