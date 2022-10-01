/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SelectionPanel.java
 *
 * Created on 14.06.2010, 16:52:22
 */
package com.jme3.gde.materials.multiview.widgets;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.properties.TexturePropertyEditor;
import com.jme3.gde.core.properties.preview.TexturePreview;
import com.jme3.gde.materials.MaterialProperty;
import com.jme3.gde.materials.multiview.MaterialEditorTopComponent;
import com.jme3.gde.materials.multiview.widgets.icons.Icons;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TexturePanel is a row in the material editor representing a special texture
 * @author normenhansen
 */
public class TexturePanel extends MaterialPropertyWidget {

    private final TexturePropertyEditor editor;
    private final ProjectAssetManager manager;
    private boolean flip = false;
    private boolean repeat = false;
    protected String textureName = null; // always enclosed with ""
    private TexturePreview texPreview;
    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    /** Creates new form SelectionPanel */
    public TexturePanel(ProjectAssetManager manager) {
        this.manager = manager;
        editor = new TexturePropertyEditor(manager);
        initComponents();
    }

    private void displayPreview() {
        if (!"".equals(textureName)) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        if (texPreview == null) {
                            texPreview = new TexturePreview(manager);
                        }
                        texPreview.requestPreview(stripQuotes(textureName), "", 80, 25, texturePreview, null);
                    } catch (AssetNotFoundException a) {
                        Logger.getLogger(MaterialEditorTopComponent.class.getName()).log(Level.WARNING, "Could not load texture {0}", textureName);
                    }
                }
            });
        }
    }
    
    private String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    private void updateFlipRepeat() {
        if (flip && repeat) {
            property.setValue("Flip Repeat " + textureName);
            texturePreview.setToolTipText("Flip Repeat " + textureName);
        } else if (flip) {
            property.setValue("Flip " + textureName);
            texturePreview.setToolTipText("Flip " + textureName);
        } else if (repeat) {
            property.setValue("Repeat " + textureName);
            texturePreview.setToolTipText("Repeat " + textureName);
        } else {
            property.setValue(textureName);
            texturePreview.setToolTipText(textureName);
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage) {
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        float ratio = (float) originalImage.getWidth() / (float) originalImage.getHeight();
        int width = 80;
        int height = 25;
        if (ratio <= 1) {
            height = (int) ((float) width * ratio);
        } else {
            width = (int) ((float) height * ratio);
        }
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        texturePreview = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(32767, 45));
        setPreferredSize(new java.awt.Dimension(467, 45));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jLabel1.text")); // NOI18N
        jLabel1.setMinimumSize(new java.awt.Dimension(70, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 16));

        jPanel1.setPreferredSize(new java.awt.Dimension(10, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jButton1.setIcon(Icons.textureAdd);
        jButton1.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.setIconTextGap(2);
        jButton1.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setFocusable(false);
        jCheckBox1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jCheckBox2.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jCheckBox2.text")); // NOI18N
        jCheckBox2.setFocusable(false);
        jCheckBox2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jButton2.setIcon(Icons.textureRemove);
        jButton2.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        texturePreview.setText(org.openide.util.NbBundle.getMessage(TexturePanel.class, "TexturePanel.texturePreview.text")); // NOI18N
        texturePreview.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        texturePreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        texturePreview.setFocusable(false);
        texturePreview.setIconTextGap(0);
        texturePreview.setMaximumSize(new java.awt.Dimension(75, 25));
        texturePreview.setMinimumSize(new java.awt.Dimension(75, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(texturePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 238, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 238, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(texturePreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 55, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 40, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Component view = editor.getCustomEditor();
        view.setVisible(true);
        if (editor.getValue() != null) {
            textureName = "\"" + editor.getAsText() + "\"";
            displayPreview();
            updateFlipRepeat();
            fireChanged();
        } else { // "No Texture" has been clicked
            textureName = "\"\"";
            texturePreview.setIcon(null);
            texturePreview.setToolTipText("");
            property.setValue("");
            fireChanged();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        textureName = "\"\"";
        texturePreview.setIcon(null);
        texturePreview.setToolTipText("");
        property.setValue("");
        fireChanged();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (property == null) {
            return;
        }
        flip = jCheckBox1.isSelected();
        updateFlipRepeat();
        fireChanged();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if (property == null) {
            return;
        }
        repeat = jCheckBox2.isSelected();
        updateFlipRepeat();
        fireChanged();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    @Override
    protected void readProperty() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (property.getValue().startsWith("Flip Repeat ")) {
                    flip = true;
                    repeat = true;
                    textureName = property.getValue().replaceFirst("Flip Repeat ", "").trim();
                } else if (property.getValue().startsWith("Flip ")) {
                    flip = true;
                    textureName = property.getValue().replaceFirst("Flip ", "").trim();
                } else if (property.getValue().startsWith("Repeat ")) {
                    repeat = true;
                    textureName = property.getValue().replaceFirst("Repeat ", "").trim();
                } else {
                    textureName = property.getValue();
                }
                jLabel1.setText(property.getName());
                jLabel1.setToolTipText(property.getName());
                displayPreview();
                texturePreview.setToolTipText(property.getValue());
                MaterialProperty prop = property;
                property = null;
                jCheckBox1.setSelected(flip);
                jCheckBox2.setSelected(repeat);
                property = prop;
            }
        });
    }

    @Override
    public void cleanUp() {
        if (texPreview != null) {
            texPreview.cleanUp();
        }
        exec.shutdownNow();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel texturePreview;
    // End of variables declaration//GEN-END:variables
}
