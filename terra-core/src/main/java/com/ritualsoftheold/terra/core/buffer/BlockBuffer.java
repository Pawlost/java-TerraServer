package com.ritualsoftheold.terra.core.buffer;

import com.ritualsoftheold.terra.core.material.TerraObject;

/**
 * Allows fast access to block data in one chunk or an octree node whose scale
 * equals to chunk scale.
 *
 */
public interface BlockBuffer extends AutoCloseable {
    
    /**
     * Sets current index to given index.
     * @param index New index.
     */
    void seek(int index);
    
    /**
     * Gets current position in buffer.
     * @return Current position.
     */
    int position();
    
    /**
     * Goes to next block.
     */
    void next();
    
    /**
     * Checks if there is a next block.
     * @return True if there is a next block, false otherwise.
     */
    boolean hasNext();
    
    /**
     * Sets material of current block.
     * @param material New material.
     */
    void write(TerraObject material);
    
    /**
     * Gets material of current block.
     * @return Material of block.
     */
    TerraObject read();

    /**
     * Gets material of requested block.
     * @return Material of block.
     */
    TerraObject get(int index);


        /**
         * Gets the reference that current block has, or null if it has none.
         * @return Reference of the block or null.
         */
    Object readRef();
    
    /**
     * Sets reference of current block.
     * @param ref New reference.
     */
    void writeRef(Object ref);
    
    @Override
    void close();
}
