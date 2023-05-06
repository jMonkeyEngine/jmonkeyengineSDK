/*
 *  Copyright (c) 2009-2023 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.assetbrowser;

import com.jme3.asset.MaterialKey;
import com.jme3.gde.assetbrowser.widgets.AssetPreviewWidget;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.editor.icons.Icons;
import com.jme3.gde.core.icons.IconList;
import com.jme3.gde.core.scene.PreviewRequest;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import jme3tools.converters.ImageToAwt;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author rickard
 */
public class PreviewUtil {

    private static final int previewSize = 150;
    private ProjectAssetManager assetManager;

    public PreviewUtil(ProjectAssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Icon getOrCreateTexturePreview(String asset, int size) {
        final var icon = tryGetPreview(asset, size);
        if (icon != null) {
            return icon;
        }
        System.out.println("creating preview ");
        Texture texture = assetManager.loadTexture(asset);
        Image image = texture.getImage();

        BufferedImage buff = ImageToAwt.convert(image, false, false, 0);

        BufferedImage scaled = scaleDown(buff, 150, 150);
        BufferedImage noAlpha = convertImage(scaled);
        savePreview(assetManager, asset.split("\\.")[0], noAlpha);
        return new ImageIcon(noAlpha);
    }

    public Icon getOrCreateMaterialPreview(String asset, AssetPreviewWidget widget, int size) {
        final var icon = tryGetPreview(asset, size);
        if (icon != null) {
            return icon;
        }

        Material mat = assetManager.loadMaterial(asset);

        Box boxMesh = new Box(1.75f, 1.75f, 1.75f);
        Geometry box = new Geometry("previewBox", boxMesh);
        box.setMaterial(mat);
        PreviewListener listener = new PreviewListener(assetManager, mat.getAssetName().split("\\.")[0], widget);
        SceneApplication.getApplication().addSceneListener(listener);
        SceneApplication.getApplication().enqueue(() -> {
            SceneApplication.getApplication().getRenderManager().preloadScene(box);
            java.awt.EventQueue.invokeLater(() -> {

                box.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.DEG_TO_RAD * 30, Vector3f.UNIT_X).multLocal(new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y)));
                MikktspaceTangentGenerator.generate(box);
                System.out.println("preview requested " + asset);
                PreviewRequest request = new PreviewRequest(listener, box, 150, 150);
                request.getCameraRequest().setLocation(new Vector3f(0, 0, 7));
                request.getCameraRequest().setLookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
                SceneApplication.getApplication().createPreview(request);
            });
        });
        return IconList.asset;
    }

    private Icon tryGetPreview(String asset, int size) {
        final var assetPath = assetManager.getAbsoluteAssetPath(asset);

        FileTime assetModificationTime = getAssetModificationTime(assetPath);

        File previewFile = loadPreviewFile(assetManager, asset.split("\\.")[0]);

        if (previewFile != null && assetModificationTime != null) {
            Path previewPath = previewFile.toPath();
            try {
                BasicFileAttributes previewAttributes = Files.readAttributes(
                        previewPath, BasicFileAttributes.class);
                FileTime previewCreationTime = previewAttributes.creationTime();

                if (previewCreationTime.compareTo(assetModificationTime) > 0) {
                    System.out.println("existing preview OK " + previewFile);
                    BufferedImage image = ImageIO.read(previewFile);
                    if (image != null) {
                        return new ImageIcon(size != previewSize ? image.getScaledInstance(size, size, 0) : image);
                    }
                    System.out.println("previewFile is null " + previewFile);
                }

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public Icon getOrCreateModelPreview(String asset, AssetPreviewWidget widget, int size) {
        final var icon = tryGetPreview(asset, size);
        if (icon != null) {
            return icon;
        }

        Material unshaded = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        unshaded.setColor("Color", ColorRGBA.Red);

        Spatial spatial = assetManager.loadModel(asset);

        recurseApplyDefaultMaterial(spatial, unshaded);

        PreviewListener listener = new PreviewListener(assetManager, asset.split("\\.")[0], widget);
        SceneApplication.getApplication().addSceneListener(listener);
        SceneApplication.getApplication().enqueue(() -> {
            SceneApplication.getApplication().getRenderManager().preloadScene(spatial);
            java.awt.EventQueue.invokeLater(() -> {
                System.out.println("preview requested " + asset);
                PreviewRequest request = new PreviewRequest(listener, spatial, 150, 150);
                request.getCameraRequest().setLocation(new Vector3f(4, 4, 7));
                request.getCameraRequest().setLookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
                SceneApplication.getApplication().createPreview(request);
            });
        });
        return IconList.asset;
    }

    private void recurseApplyDefaultMaterial(Spatial spatial, Material material) {
        if (spatial instanceof Node) {
            ((Node) spatial).getChildren().forEach(child -> recurseApplyDefaultMaterial(child, material));
        } else if (spatial instanceof Geometry) {
            if (((Geometry) spatial).getMaterial() == null) {
                spatial.setMaterial(material);
            }
        }
    }

    private FileTime getAssetModificationTime(String assetPath) {
        if (assetPath == null) {
            return null;
        }
        Path path = new File(assetPath).toPath();

        try {
            // creating BasicFileAttributes class object using
            // readAttributes method
            BasicFileAttributes file_att = Files.readAttributes(
                    path, BasicFileAttributes.class);
            return file_att.lastModifiedTime();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private File loadPreviewFile(ProjectAssetManager assetManager, String id) {
        FileObject fileObject = assetManager.getProject().getProjectDirectory();
        return new File(fileObject.getPath() + "/.assetBrowser/", id + ".jpg");
    }

    private void savePreview(ProjectAssetManager assetManager, String id, BufferedImage preview) {
        FileObject fileObject = assetManager.getProject().getProjectDirectory();
        String[] fileSections = id.split("/");
        String fileName = fileSections[fileSections.length - 1];
        File path = new File(fileObject.getPath() + "/.assetBrowser/" + id.substring(0, id.length() - fileName.length()));
        File file = new File(path, fileName + ".jpg");
        try {
            path.mkdirs();
            file.createNewFile();
            ImageIO.write(preview, "jpg", file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private BufferedImage scaleDown(BufferedImage sourceImage, int targetWidth, int targetHeight) {
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, sourceImage.getType());

        Graphics2D g = targetImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, 0, 0, sourceWidth, sourceHeight, null);
        g.dispose();

        return targetImage;
    }

    private class PreviewListener implements SceneListener {

        final AssetPreviewWidget widget;
        final ProjectAssetManager assetManager;
        private String assetName;

        public PreviewListener(ProjectAssetManager assetManager, String assetName, AssetPreviewWidget widget) {
            this.widget = widget;
            this.assetManager = assetManager;
            this.assetName = assetName;
        }

        @Override
        public void sceneOpened(SceneRequest request) {
        }

        @Override
        public void sceneClosed(SceneRequest request) {
        }

        @Override
        public void previewCreated(PreviewRequest request) {
            if (request.getRequester() == this) {
                final var image = convertImage(request.getImage());
                java.awt.EventQueue.invokeLater(() -> {
                    widget.setPreviewImage(new ImageIcon(image));
                    savePreview(assetManager, assetName, image);
                    widget.revalidate();
                });
            }
        }
    };

    private static BufferedImage convertImage(BufferedImage file) {
        final int width = file.getWidth();
        final int height = file.getHeight();
        BufferedImage background = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = background.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.drawImage(file, 0, 0, null);
        g.dispose();
        return background;
    }
}
