package com.davejy.modelsimplification;

/**
 * Created by lenovo on 2017/4/26.
 */

class MatrixHelper {

    /**
     * 将两个4X4的矩阵相加，结果保存在resMat中
     * @param resMat
     * @param lhsMat
     * @param rhsMat
     */
    public static void add(float[] resMat, float[] lhsMat, float[] rhsMat) {
        for (int i = 0; i < 16; i++) {
            resMat[i] = lhsMat[i] + rhsMat[i];
        }
    }

    /**
     * 将4X4矩阵与常数相乘，结果保存在resMat 中
     * @param resMat
     * @param mat
     * @param k
     */
    public static void multiplyK(float[] resMat, float[] mat, float k) {
        for (int i = 0; i < 16; i++) {
            resMat[i] = mat[i] * k;
        }
    }

    /**
     * 点乘两个4维向量
     * @param vec1
     * @param vec2
     * @return
     */
    public static float dotProduct(float[] vec1, float[] vec2) {
        float res = 0;
        for (int i = 0; i < 4; i++) {
            res += vec1[i] * vec2[i];
        }
        return res;
    }
    
    /**
     * 矩阵求逆，修改自android.opengl.Matrix类
     * @param mInv
     * @param m
     * @return
     */
    public static boolean invertM(float[] mInv, float[] m) {
        // Invert a 4 x 4 matrix using Cramer's Rule

        // transpose matrix
        final float src0  = m[0];
        final float src4  = m[1];
        final float src8  = m[2];
        final float src12 = m[3];

        final float src1  = m[4];
        final float src5  = m[5];
        final float src9  = m[6];
        final float src13 = m[7];

        final float src2  = m[8];
        final float src6  = m[9];
        final float src10 = m[10];
        final float src14 = m[11];

        final float src3  = m[12];
        final float src7  = m[13];
        final float src11 = m[14];
        final float src15 = m[15];

        // calculate pairs for first 8 elements (cofactors)
        final float atmp0  = src10 * src15;
        final float atmp1  = src11 * src14;
        final float atmp2  = src9  * src15;
        final float atmp3  = src11 * src13;
        final float atmp4  = src9  * src14;
        final float atmp5  = src10 * src13;
        final float atmp6  = src8  * src15;
        final float atmp7  = src11 * src12;
        final float atmp8  = src8  * src14;
        final float atmp9  = src10 * src12;
        final float atmp10 = src8  * src13;
        final float atmp11 = src9  * src12;

        // calculate first 8 elements (cofactors)
        final float dst0  = (atmp0 * src5 + atmp3 * src6 + atmp4  * src7)
                          - (atmp1 * src5 + atmp2 * src6 + atmp5  * src7);
        final float dst1  = (atmp1 * src4 + atmp6 * src6 + atmp9  * src7)
                          - (atmp0 * src4 + atmp7 * src6 + atmp8  * src7);
        final float dst2  = (atmp2 * src4 + atmp7 * src5 + atmp10 * src7)
                          - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
        final float dst3  = (atmp5 * src4 + atmp8 * src5 + atmp11 * src6)
                          - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
        final float dst4  = (atmp1 * src1 + atmp2 * src2 + atmp5  * src3)
                          - (atmp0 * src1 + atmp3 * src2 + atmp4  * src3);
        final float dst5  = (atmp0 * src0 + atmp7 * src2 + atmp8  * src3)
                          - (atmp1 * src0 + atmp6 * src2 + atmp9  * src3);
        final float dst6  = (atmp3 * src0 + atmp6 * src1 + atmp11 * src3)
                          - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
        final float dst7  = (atmp4 * src0 + atmp9 * src1 + atmp10 * src2)
                          - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);

        // calculate pairs for second 8 elements (cofactors)
        final float btmp0  = src2 * src7;
        final float btmp1  = src3 * src6;
        final float btmp2  = src1 * src7;
        final float btmp3  = src3 * src5;
        final float btmp4  = src1 * src6;
        final float btmp5  = src2 * src5;
        final float btmp6  = src0 * src7;
        final float btmp7  = src3 * src4;
        final float btmp8  = src0 * src6;
        final float btmp9  = src2 * src4;
        final float btmp10 = src0 * src5;
        final float btmp11 = src1 * src4;

        // calculate second 8 elements (cofactors)
        final float dst8  = (btmp0  * src13 + btmp3  * src14 + btmp4  * src15)
                          - (btmp1  * src13 + btmp2  * src14 + btmp5  * src15);
        final float dst9  = (btmp1  * src12 + btmp6  * src14 + btmp9  * src15)
                          - (btmp0  * src12 + btmp7  * src14 + btmp8  * src15);
        final float dst10 = (btmp2  * src12 + btmp7  * src13 + btmp10 * src15)
                          - (btmp3  * src12 + btmp6  * src13 + btmp11 * src15);
        final float dst11 = (btmp5  * src12 + btmp8  * src13 + btmp11 * src14)
                          - (btmp4  * src12 + btmp9  * src13 + btmp10 * src14);
        final float dst12 = (btmp2  * src10 + btmp5  * src11 + btmp1  * src9 )
                          - (btmp4  * src11 + btmp0  * src9  + btmp3  * src10);
        final float dst13 = (btmp8  * src11 + btmp0  * src8  + btmp7  * src10)
                          - (btmp6  * src10 + btmp9  * src11 + btmp1  * src8 );
        final float dst14 = (btmp6  * src9  + btmp11 * src11 + btmp3  * src8 )
                          - (btmp10 * src11 + btmp2  * src8  + btmp7  * src9 );
        final float dst15 = (btmp10 * src10 + btmp4  * src8  + btmp9  * src9 )
                          - (btmp8  * src9  + btmp11 * src10 + btmp5  * src8 );

        // calculate determinant
        final float det =
                src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;

        if (det == 0.0f) {
            return false;
        }

        // calculate matrix inverse
        final float invdet = 1.0f / det;
        mInv[ 0 ] = dst0  * invdet;
        mInv[ 1 ] = dst1  * invdet;
        mInv[ 2 ] = dst2  * invdet;
        mInv[ 3 ] = dst3  * invdet;

        mInv[ 4 ] = dst4  * invdet;
        mInv[ 5 ] = dst5  * invdet;
        mInv[ 6 ] = dst6  * invdet;
        mInv[ 7 ] = dst7  * invdet;

        mInv[ 8 ] = dst8  * invdet;
        mInv[ 9 ] = dst9  * invdet;
        mInv[10 ] = dst10 * invdet;
        mInv[11 ] = dst11 * invdet;

        mInv[12 ] = dst12 * invdet;
        mInv[13 ] = dst13 * invdet;
        mInv[14 ] = dst14 * invdet;
        mInv[15 ] = dst15 * invdet;

        return true;
    }
    
    /**
     * 将4X4的矩阵与1X4的列向量相乘
     * @param resultVec
     * @param lhsMat
     * @param rhsVec
     */
    public static void multiplyMV(float[] resultVec, float[] lhsMat, float[] rhsVec) {
    	for (int i = 0; i < 4; i++) {
    		for (int j = 0; j < 4; j++) {
				resultVec[i] += lhsMat[i + j * 4] * rhsVec[j];
			}
    	}
    }
}
