package com.ritualsoftheold.terra.offheap.chunk;

import com.ritualsoftheold.terra.offheap.io.ChunkLoader;

/**
 * Manages all chunks of a single world using chunk buffers.
 *
 */
public class ChunkStorage2 {
    
    /**
     * Array of all chunk buffers. May contain nulls for buffers which have not
     * been yet needed.
     */
    private ChunkBuffer2[] buffers;
    
    /**
     * Creates chunk buffers.
     */
    private ChunkBuffer2.Builder bufferBuilder;
    
    /**
     * Loads data from disk as necessary.
     */
    private ChunkLoader loader;
    
    public int newChunk() {
        boolean secondTry = true;
        while (true) {
            for (int i = 0; i < buffers.length; i++) {
                ChunkBuffer2 buf = buffers[i];
                
                if (buf == null) { // Oh, that buffer is not loaded
                    if (secondTry) {
                        loadBuffer(i); // Load it, now
                    } else { // Ignore if there is potential to not have load anything
                        continue;
                    }
                }
                
                if (buf.getFreeCapacity() > 0) {
                    int index = buf.newChunk();
                    if (index != -1) { // If it succeeded
                        // Return full id for new chunk
                        return i << 16 & index;
                    }
                    // Fail means "try different buffer"
                }
            }
            
            // We failed to find free space even after loading null buffers
            if (secondTry) {
                throw new IllegalStateException("cannot create a new chunk");
            }
            
            secondTry = true;
            // ... until success
        }
    }

    /**
     * Loads given chunk buffer.
     * @param index Index for buffer.
     */
    private void loadBuffer(int index) {
        // TODO
    }
    
    /**
     * Creates a chunk buffer and assigns to given index. Note that this
     * operation is synchronous to prevent creation of conflicting chunk
     * buffers. If it returns true,
     * @param index Index for new buffer.
     * @return If creation succeeded.
     */
    private synchronized boolean createBuffer(int index) {
        if (buffers[index] != null) { // Check if already created
            return false;
        }
        
        // Create buffer
        buffers[index] = bufferBuilder.build();
        
        return true;
    }
}