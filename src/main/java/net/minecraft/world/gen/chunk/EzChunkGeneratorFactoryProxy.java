//a workaround for a package private interface
package net.minecraft.world.gen.chunk;

import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;

public class EzChunkGeneratorFactoryProxy<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> {
    public interface ProxyFactory<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>>{
        T createProxy(World w, BiomeSource biomesource, C gensettings);
    }
    private ProxyFactory<C,T> proxy;

    private class factoryClass implements ChunkGeneratorFactory<C,T>{

        @Override
        public T create(World var1, BiomeSource var2, C var3) {
            return proxy.createProxy(var1, var2, var3);
        }
    }

    public EzChunkGeneratorFactoryProxy(ProxyFactory<C,T> proxy){
        this.proxy = proxy;
    }

    public ChunkGeneratorFactory<C,T> getFactory(){
        return new factoryClass();
    }

}
