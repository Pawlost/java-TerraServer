package com.ritualsoftheold.terra.offheap;

import com.ritualsoftheold.terra.core.material.TerraModule;
import com.ritualsoftheold.terra.core.material.Registry;
import com.ritualsoftheold.terra.offheap.chunk.ChunkLArray;

/**
 * Implementations of this generate the world from scratch.
 *
 */
public interface WorldGeneratorInterface {
    WorldGeneratorInterface setup(Registry registry, TerraModule mod);
    void generate(ChunkLArray chunk);
}
