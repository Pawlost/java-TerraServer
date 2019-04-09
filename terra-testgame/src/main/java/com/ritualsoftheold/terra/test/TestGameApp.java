package com.ritualsoftheold.terra.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.system.AppSettings;
import com.ritualsoftheold.terra.core.TerraModule;
import com.ritualsoftheold.terra.offheap.chunk.ChunkBuffer;
import com.ritualsoftheold.terra.core.gen.interfaces.world.WorldGeneratorInterface;
import com.ritualsoftheold.terra.core.gen.objects.LoadMarker;
import com.ritualsoftheold.terra.core.material.MaterialRegistry;
import com.ritualsoftheold.terra.core.material.TerraTexture;
import com.ritualsoftheold.terra.offheap.memory.MemoryPanicHandler;
import com.ritualsoftheold.terra.mesher.resource.TextureManager;
import com.ritualsoftheold.terra.offheap.node.OffheapChunk;
import com.ritualsoftheold.terra.offheap.world.OffheapWorld;
import com.ritualsoftheold.terra.world.WorldGenerator;
import com.ritualsoftheold.terra.offheap.world.WorldLoadListener;
import com.ritualsoftheold.terra.mesher.MeshContainer;
import com.ritualsoftheold.terra.mesher.NaiveMesher;
import com.ritualsoftheold.terra.mesher.VoxelMesher;
import com.ritualsoftheold.terra.offheap.io.dummy.DummyChunkLoader;
import com.ritualsoftheold.terra.offheap.io.dummy.DummyOctreeLoader;

import io.netty.buffer.ByteBufAllocator;

public class TestGameApp extends SimpleApplication implements ActionListener {

    private OffheapWorld world;
    private LoadMarker player;
    boolean wireframe = false;
    ArrayList<Material> materials = null;

    private BlockingQueue<Geometry> geomCreateQueue = new ArrayBlockingQueue<>(10000);

    private float loadMarkersUpdated;

    public static void main(String... args) {
        TestGameApp app = new TestGameApp();
        app.showSettings = false;
        app.settings = new AppSettings(true);
        app.settings.setResolution(1024, 768);
        app.settings.setTitle("Terra testgame");
        app.settings.setFullscreen(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //setDisplayFps(false);
        //setDisplayStatView(false);
        materials = new ArrayList<>();

        TerraModule mod = new TerraModule("testgame");
        mod.newMaterial().name("dirt").texture(new TerraTexture(256, 256, "NorthenForestDirt256px.png"));
        mod.newMaterial().name("grass").texture(new TerraTexture(256, 256, "NorthenForestGrass256px.png"));
        MaterialRegistry reg = new MaterialRegistry();
        mod.registerMaterials(reg);

        WorldGeneratorInterface<?> gen = new WorldGenerator();
        gen.setup(0, reg);

        ChunkBuffer.Builder bufferBuilder = new ChunkBuffer.Builder()
                .maxChunks(128)
                .queueSize(4);

        world = new OffheapWorld.Builder()
                .chunkLoader(new DummyChunkLoader())
                .octreeLoader(new DummyOctreeLoader(32768))
                .storageExecutor(ForkJoinPool.commonPool())
                .chunkStorage(bufferBuilder, 128)
                .octreeStorage(32768)
                .generator(gen)
                .generatorExecutor(ForkJoinPool.commonPool())
                .materialRegistry(reg)
                .memorySettings(10000000, 10000000, new MemoryPanicHandler() {

                    @Override
                    public PanicResult outOfMemory(long max, long used, long possible) {
                        return PanicResult.CONTINUE;
                    }

                    @Override
                    public PanicResult goalNotMet(long goal, long possible) {
                        return PanicResult.CONTINUE;
                    }
                })
                .build();

        player = world.createLoadMarker(0,0, 0, 32, 32, 0);
       // LoadMarker secondchunk = world.createLoadMarker(56+16+32,0, 56+16+32, 32, 32, 0);

        world.addLoadMarker(player);
      //  world.addLoadMarker(secondchunk);

        TextureManager texManager = new TextureManager(assetManager); // Initialize texture atlas/array manager
        texManager.loadMaterials(reg); // And make it load material registry

        VoxelMesher mesher = new NaiveMesher();

        world.setLoadListener(new WorldLoadListener() {

            @Override
            public void octreeLoaded(long addr, long groupAddr, int id, float x,
                                     float y, float z, float scale, LoadMarker trigger) {
                // For now, just ignore octrees
            }

            @Override
            public void chunkLoaded(OffheapChunk chunk, float x, float y, float z, LoadMarker trigger) {
                Vector3f center = cam.getLocation();
                if (Math.abs(x - center.x) > 128
                        || Math.abs(y - center.y) > 128
                        || Math.abs(z - center.z) > 128) {
                    //System.out.println("too far away: " + x + ", " + y + ", " + z);
                    return;
                }

                //System.out.println("Loaded chunk: " + chunk.memoryAddress());
                MeshContainer container = new MeshContainer(200, ByteBufAllocator.DEFAULT);
                mesher.chunk(chunk.getBuffer(), texManager, container);

                // Create mesh
                Mesh mesh = new Mesh();

                //System.out.println(mesher.getVertices());
                //System.out.println(mesher.getIndices());
                mesh.setBuffer(Type.Position, 1, container.getVertices().nioBuffer().asFloatBuffer());
                mesh.setBuffer(Type.Index, 3, container.getIndices().nioBuffer().asIntBuffer());
                mesh.setBuffer(Type.TexCoord, 2, container.getTextureCoordinates().nioBuffer().asFloatBuffer());

                // Create geometry
                Geometry geom = new Geometry("chunk:" + x + "," + y + "," + z, mesh);
                //mat.setParam("SeparateTexCoord", VarType.Boolean, true);

                // Create material
                materials.add(new Material(assetManager, "terra/shader/TerraArray.j3md"));

                materials.get(materials.size() - 1).setTexture("ColorMap", texManager.convertTexture(container.getTexture()));
                geom.setMaterial(materials.get(materials.size() - 1));
                //geom.setLocalScale(0.5f);
                geom.setLocalTranslation(x, y, z);
                geom.setCullHint(CullHint.Never);

                container.release();
                // Place geometry in queue for main thread
                geomCreateQueue.add(geom);
            }
        });

        // Some config options
        flyCam.setMoveSpeed(10);
        rootNode.setCullHint(CullHint.Never);

        List<CompletableFuture<Void>> markers = world.updateLoadMarkers();
        markers.forEach((f) -> {
            f.join();
        });

        inputManager.addMapping("RELOAD", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addListener(this, "RELOAD");
        inputManager.addMapping("toggle wireframe", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(this, "toggle wireframe");

        rootNode.addLight(new AmbientLight());
    }

    @Override
    public void simpleUpdate(float tpf) {
        loadMarkersUpdated += tpf;
        if (loadMarkersUpdated > 1) {
            loadMarkersUpdated = 0;
            Vector3f camLoc = cam.getLocation();
            //System.out.println(camLoc);
            player.move(camLoc.getX(), camLoc.getY(), camLoc.getZ());
            //CompletableFuture.runAsync(() -> {
            //long stamp = world.enter();
            //world.updateLoadMarkers(); // Update load markers
            //world.leave(stamp);
            //});
        }

        while (!geomCreateQueue.isEmpty()) {
            Geometry geom = geomCreateQueue.poll();
            //System.out.println("create geom: " + geom.getLocalTranslation());
            rootNode.attachChild(geom);
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("RELOAD") && isPressed) {
            rootNode.detachAllChildren();
            world.updateLoadMarkers();
        }
        if (name.equals("toggle wireframe") && !isPressed) {
            wireframe = !wireframe; // toggle boolean
            for(Material material:materials) {
                material.getAdditionalRenderState().setWireframe(wireframe);
            }
        }
    }
}