/*
 *  Copyright (c) 2009-2016 jMonkeyEngine
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
package com.jme3.gde.core.sceneexplorer.nodes;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.gde.core.icons.IconList;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.editor.GravityPropertyEditor;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service = SceneExplorerNode.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class JmeVehicleControl extends JmeControl {

    private static Image smallImage = IconList.vehicle.getImage();
    private VehicleControl vehicle;

    public JmeVehicleControl() {
    }

    public JmeVehicleControl(VehicleControl vehicle, Children children) {
        super(children);
        getLookupContents().add(vehicle);
        getLookupContents().add(this);
        this.vehicle = vehicle;
        control = vehicle;
        setName("VehicleControl");
    }

    @Override
    public Image getIcon(int type) {
        return smallImage;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return smallImage;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("VehicleControl");
        set.setName(VehicleControl.class.getName());
        VehicleControl obj = vehicle;//getLookup().lookup(Spatial.class);
        if (obj == null) {
            return sheet;
        }

        set.put(makeProperty(obj, Vector3f.class, "getPhysicsLocation", "setPhysicsLocation", "Physics Location"));
        set.put(makeProperty(obj, Quaternion.class, "getPhysicsRotation", "setPhysicsRotation", "Physics Rotation"));

        set.put(makeProperty(obj, CollisionShape.class, "getCollisionShape", "setCollisionShape", "Collision Shape"));
        set.put(makeProperty(obj, int.class, "getCollisionGroup", "setCollisionGroup", "Collision Group"));
        set.put(makeProperty(obj, int.class, "getCollideWithGroups", "setCollideWithGroups", "Collide With Groups"));

        set.put(makeProperty(obj, float.class, "getFriction", "setFriction", "Friction"));
        set.put(makeProperty(obj, float.class, "getMass", "setMass", "Mass"));
        set.put(makeProperty(obj, boolean.class, "isKinematic", "setKinematic", "Kinematic"));
        set.put(createGravityProperty(obj));
        set.put(makeProperty(obj, float.class, "getLinearDamping", "setLinearDamping", "Linear Damping"));
        set.put(makeProperty(obj, float.class, "getAngularDamping", "setAngularDamping", "Angular Damping"));
        set.put(makeProperty(obj, float.class, "getRestitution", "setRestitution", "Restitution"));

        set.put(makeProperty(obj, float.class, "getLinearSleepingThreshold", "setLinearSleepingThreshold", "Linear Sleeping Threshold"));
        set.put(makeProperty(obj, float.class, "getAngularSleepingThreshold", "setAngularSleepingThreshold", "Angular Sleeping Threshold"));

        set.put(makeProperty(obj, float.class, "getFrictionSlip", "setFrictionSlip", "Friction Slip"));
        set.put(makeProperty(obj, float.class, "getMaxSuspensionTravelCm", "setMaxSuspensionTravelCm", "Max Suspension Travel Cm"));
        set.put(makeProperty(obj, float.class, "getMaxSuspensionForce", "setMaxSuspensionForce", "Max Suspension Force"));
        set.put(makeProperty(obj, float.class, "getSuspensionCompression", "setSuspensionCompression", "Suspension Compression"));
        set.put(makeProperty(obj, float.class, "getSuspensionDamping", "setSuspensionDamping", "Suspension Damping"));
        set.put(makeProperty(obj, float.class, "getSuspensionStiffness", "setSuspensionStiffness", "Suspension Stiffness"));

        sheet.put(set);
        return sheet;

    }

    @Override
    public Class getExplorerObjectClass() {
        return VehicleControl.class;
    }

    @Override
    public Class getExplorerNodeClass() {
        return JmeVehicleControl.class;
    }

    @Override
    public org.openide.nodes.Node[] createNodes(Object key, DataObject key2, boolean cookie) {
        PhysicsVehicleChildren children = new PhysicsVehicleChildren((VehicleControl) key);
        children.setReadOnly(cookie);
        children.setDataObject(key2);
        return new org.openide.nodes.Node[]{new JmeVehicleControl((VehicleControl) key, children).setReadOnly(cookie)};
    }

    public static class PhysicsVehicleChildren extends JmeSpatialChildren {

        VehicleControl control;

        public PhysicsVehicleChildren(VehicleControl control) {
            this.control = control;
        }

        @Override
        public void refreshChildren(boolean immediate) {
            setKeys(createKeys());
            refresh();
        }

        @Override
        protected List<Object> createKeys() {
            try {
                return SceneApplication.getApplication().enqueue(new Callable<List<Object>>() {

                    public List<Object> call() throws Exception {
                        List<Object> keys = new LinkedList<Object>();
                        for (int i = 0; i < control.getNumWheels(); i++) {
                            keys.add(control.getWheel(i));
                        }
                        return keys;
                    }
                }).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void setReadOnly(boolean cookie) {
            this.readOnly = cookie;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(createKeys());
        }

        @Override
        protected Node[] createNodes(Object key) {
            if (key instanceof VehicleWheel) {
                VehicleWheel assetKey = (VehicleWheel) key;
                return new Node[]{new JmeVehicleWheel(control, assetKey)};
            }
            return null;
        }
    }
    
    private Property createGravityProperty(PhysicsRigidBody physicsRigidBody) {
        return new PropertySupport("gravity", Vector3f.class, "Gravity", "Set the gravity vector", true, true) {
            private final GravityPropertyEditor editor = new GravityPropertyEditor(physicsRigidBody);

            @Override
            public PropertyEditor getPropertyEditor() {
                return editor;
            }

            @Override
            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                return editor.getValue();
            }

            @Override
            public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                editor.setValue(t);
            }
        };
    }
}
