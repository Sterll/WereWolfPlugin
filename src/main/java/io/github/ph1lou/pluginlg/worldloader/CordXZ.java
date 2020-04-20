package io.github.ph1lou.pluginlg.worldloader;


// simple storage class for chunk x/z values
public class CordXZ {
    public int x, z;

    public CordXZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    // transform values between block, chunk, and region

    public static int blockToChunk(int blockVal) {    // 1 chunk is 16x16 blocks
        return blockVal >> 4;   // ">>4" == "/16"
    }
	public static int chunkToRegion(int chunkVal)
	{	// 1 region is 32x32 chunks
		return chunkVal >> 5;   // ">>5" == "/32"
	}
	public static int chunkToBlock(int chunkVal)
	{
		return chunkVal << 4;   // "<<4" == "*16"
	}


	@Override
	public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj == null || obj.getClass() != this.getClass())
            return false;

        CordXZ test = (CordXZ) obj;
        return test.x == this.x && test.z == this.z;
    }

	@Override
	public int hashCode()
	{
		return (this.x << 9) + this.z;
	}
}