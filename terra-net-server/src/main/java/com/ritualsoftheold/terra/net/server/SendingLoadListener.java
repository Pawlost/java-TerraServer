package com.ritualsoftheold.terra.net.server;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import com.ritualsoftheold.terra.net.TerraProtocol;
import com.ritualsoftheold.terra.offheap.node.OffheapChunk;
import com.ritualsoftheold.terra.offheap.world.OffheapWorld;
import com.ritualsoftheold.terra.offheap.world.WorldLoadListener;
import com.ritualsoftheold.terra.world.LoadMarker;

import io.aeron.Publication;

/**
 * Sends world data to onservers.
 *
 */
public class SendingLoadListener implements WorldLoadListener {
    
    /**
     * World which we are handling sending for.
     */
    private OffheapWorld world;
    
    /**
     * Our observers, mapped by their load markers.
     */
    private ConcurrentMap<LoadMarker,WorldObserver> markersToObservers;
    
    private Publication publication;
    
    public SendingLoadListener(OffheapWorld world) {
        this.world = world;
        this.markersToObservers = new ConcurrentHashMap<>();
    }
    
    public void addObserver(WorldObserver observer) {
        markersToObservers.put(observer.getLoadMarker(), observer);
    }
    
    public void removeObserver(WorldObserver observer) {
        markersToObservers.remove(observer.getLoadMarker());
    }

    @Override
    public void octreeLoaded(long addr, long groupAddr, int id, float x, float y, float z, float scale,
            LoadMarker trigger) {
        //System.out.println("Octree, addr: " + addr + ", scale: " + scale);
        WorldObserver observer = markersToObservers.get(trigger);
        if (observer == null) {
            return;
        }
        
        observer.octreeLoaded(addr, id, scale); // Observer does bulk sending
    }

    @Override
    public void chunkLoaded(OffheapChunk chunk, float x, float y, float z, LoadMarker trigger) {
        //System.out.println("Chunk: " + chunk.getIndex());
        WorldObserver observer = markersToObservers.get(trigger);
        if (observer == null) {
            System.out.println("no observer");
            return;
        }
        
        int len = chunk.memoryLength();
        MutableDirectBuffer msg = new UnsafeBuffer(ByteBuffer.allocateDirect(len + 9)); // 9 for header
        
        try (OffheapChunk.Storage storage = chunk.getStorage()) {
            msg.putByte(0, TerraProtocol.MESSAGE_TYPE_CHUNK);
            msg.putByte(1, storage.format.getChunkType()); // Chunk type
            msg.putInt(2, chunk.getChunkBuffer().getId() << 16 | chunk.getIndex()); // Full id of the chunk
            msg.putInt(6, len); // Chunk data length
            world.copyChunkData(chunk.getIndex(), msg.addressOffset() + 9); // World helper to copy the chunk
        }
        
        // Push data to observer
        publication.offer(msg);
    }
    
    @Override
    public void finished(LoadMarker trigger) {
        WorldObserver observer = markersToObservers.get(trigger);
        if (observer == null) {
            return;
        }
        observer.octreesFinished();
    }
}
