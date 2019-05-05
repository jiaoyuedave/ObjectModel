
package com.davejy.modelsimplification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectModel {

    public static final int MODE_DEFAULT = 0;
    public static final int MODE_QEM = 0;		// 原始QEM算法
    public static final int MODE_QEM_A = 1;		// 增加面积度量
    public static final int MODE_QEM_V = 2;		// 增加体积度量
    public static final int MODE_QEM_N = 4;		// 增加法向量度量
    private int mode = 0;

    private final List<Vertex> vertexList = new ArrayList<>();          // 顶点列表
    private final List<Face> faceList = new ArrayList<>();              // 三角面列表
    private IndexMinPQ<Float> costHeap;                               // 折叠代价的优先队列
    private int vN;                                                   // 模型中顶点的数目
    private int fN;                                                   // 模型中三角面的数目

    private String basePath;                                      // 基准路径

    public ObjectModel(String fileName) {
        try {
            loadFromObjFile(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ObjectModel(Reader reader) {
        try {
            load(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从.obj 文件加载三维模型，适用于PC端
     * @param fileName 文件路径
     * @return this
     * @throws FileNotFoundException
     */
    public ObjectModel loadFromObjFile(String fileName) throws FileNotFoundException {
        setBasePathFromFilename(fileName);

        Reader reader = new BufferedReader(new FileReader(fileName));
        return load(reader);
    }

    /**
     * 从输入流读取三维模型，android端使用此方法
     * @param reader 输入流
     * @return this
     * @throws FileNotFoundException
     */
    public ObjectModel load(Reader reader) throws FileNotFoundException {
        // ObjectFileParser does lexical analysis
        ObjectFileParser st = new ObjectFileParser(reader);

        readFile(st);

//        // 读完所有的面后才能计算顶点的平均法向量
//        for (Vertex v : vertexList) {
//            v.computeNormal();
//        }
        vN = vertexList.size();
        fN = faceList.size();
        return this;
    }

    public void simplifiedTo(int vertexNum) {
        costHeap = new IndexMinPQ<>(vertexList.size());
        computeAllCost();

        while (vN > vertexNum) {
//        	System.out.println("vertex index:" + costHeap.minIndex() + "\t" + "cost:" + costHeap.min());

            // 取出折叠代价最小的顶点
            int vIndex = costHeap.delMin();


            // 进行一次边收缩
            collapse(vIndex);
        }
    }

    public void simplifiedToRatio(float ratio) {
        if (ratio >= 1 || ratio < 0) {
            return;
        }

        simplifiedTo((int) (vN * ratio));
    }
    
    public void writeTo(OutputStream os) {
    	PrintWriter writer = new PrintWriter(os);
    	
    	for (Face face : faceList) {
    		if (face == null) {
    			continue;
    		}
    		StringBuilder stringBuilder = new StringBuilder();
    		Vertex v1 = vertexList.get(face.verticesIndex[0]);
    		stringBuilder.append(v1.position.x + " " + v1.position.y + " " + v1.position.z + " ");
    		Vertex v2 = vertexList.get(face.verticesIndex[1]);
    		stringBuilder.append(v2.position.x + " " + v2.position.y + " " + v2.position.z + " ");
    		Vertex v3 = vertexList.get(face.verticesIndex[2]);
    		stringBuilder.append(v3.position.x + " " + v3.position.y + " " + v3.position.z + " ");
    		Vector normal = face.normal;
    		stringBuilder.append(normal.x + " " + normal.y + " " + normal.z);
    		
    		writer.println(stringBuilder.toString());
    		writer.flush();
    	}
    	writer.close();
    }
    
    public void writeObjFile() {
    	try (FileWriter fw = new FileWriter("new_model.obj")) {
//			fw = new FileWriter("new_model.txt");
			String ls = System.lineSeparator();
			int[] table = new int[vertexList.size()];
			int count = 1;
			for (int i = 0; i < vertexList.size(); i++) {
				Vertex v = vertexList.get(i);
				if (v == null) continue;
				table[i] = count;
				count++;
				fw.write(String.format("v %f %f %f%s", 
						v.position.x, v.position.y, v.position.z, ls));
			}
			for (Face f : faceList) {
				if (f == null) continue;
				fw.write(String.format("f %d %d %d%s", 
						table[f.verticesIndex[0]], 
						table[f.verticesIndex[1]], 
						table[f.verticesIndex[2]], ls));
			}
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public float calQAve() {
    	float ave = 0;
    	int n = 0;
    	for (Face f : faceList) {
    		if (f == null) continue;
    		n++;
    		Vertex v1 = vertexList.get(f.verticesIndex[0]);
    		Vertex v2 = vertexList.get(f.verticesIndex[1]);
    		Vertex v3 = vertexList.get(f.verticesIndex[2]);
    		float l1 = Vector.minus(v1.position, v2.position).magnitude();
    		float l2 = Vector.minus(v2.position, v3.position).magnitude();
    		float l3 = Vector.minus(v1.position, v3.position).magnitude();
    		float q = (float) (4 * Math.sqrt(3.0) * f.area / (l1 * l1 + l2 * l2 + l3 * l3));
    		if (q > 1) q = 1;
    		if (q < 0) q = 0;
    		ave = ave + (q - ave) / n;
    	}
    	return ave;
    }
    
    public float calQMSE(float ave) {
    	float mse = 0;
    	int n = 0;
    	for (Face f : faceList) {
    		if (f == null) continue;
    		n++;
    		Vertex v1 = vertexList.get(f.verticesIndex[0]);
    		Vertex v2 = vertexList.get(f.verticesIndex[1]);
    		Vertex v3 = vertexList.get(f.verticesIndex[2]);
    		float l1 = Vector.minus(v1.position, v2.position).magnitude();
    		float l2 = Vector.minus(v2.position, v3.position).magnitude();
    		float l3 = Vector.minus(v1.position, v3.position).magnitude();
    		float q = (float) (4 * Math.sqrt(3.0) * f.area / (l1 * l1 + l2 * l2 + l3 * l3));
    		if (q > 1) q = 1;
    		if (q < 0) q = 0;
    		float e = (q - ave) * (q - ave);
    		mse = mse + (e - mse) / n;
    	}
    	return mse;
    }
    
    public void print() {
    	System.out.println(basePath);
    	for (Vertex v : vertexList) {
            if (v != null) {
				System.out.println("Vertex:" + v);
			}
        }
        for (Face f : faceList) {
            if (f != null) {
				System.out.println("Face:" + f + " Normal:" + f.normal);
			}
        }
        System.out.println("顶点： " + vN + "面： " + fN);
    }

    /**
     * 读取文件
     * @param st 解析器对象
     * @throws ParsingErrorException
     */
    void readFile(ObjectFileParser st) throws ParsingErrorException {

        st.getToken();
        while (st.ttype != ObjectFileParser.TT_EOF) {
            if (st.ttype == ObjectFileParser.TT_WORD) {
                if (st.sval.equals("v")) {
                    readVertex(st);
                } else if (st.sval.equals("f")) {
                    readFace(st);
                } else {
                    // st.skipToNextLine();
                    // throw new
                    // ParsingErrorException("Unrecognized token, line " +
                    // st.lineno());
                }
            }
            st.skipToNextLine();
            // Get next token
            st.getToken();
        }
    }

    /**
     * 读取顶点
     * @param st 解析器对象
     * @throws ParsingErrorException
     */
    void readVertex(ObjectFileParser st) throws ParsingErrorException {
        Vertex p = new Vertex();

        st.getNumber();
        p.position.x = (float) st.nval;
        st.getNumber();
        p.position.y = (float) st.nval;
        st.getNumber();
        p.position.z = (float) st.nval;

        st.skipToNextLine();

        // Add this vertex to the array
        vertexList.add(p);

//        if (BuildConfig.DEBUG && LoggerConfig.ANDROID_DEBUG) {
//            Log.d(TAG, "readVertex: " + p);
//        }
    } // End of readVertex

    void readFace(ObjectFileParser st) throws ParsingErrorException {
        ArrayList<Integer> points = new ArrayList<>();                         // 面的三个顶点的索引列表

        while (st.ttype != StreamTokenizer.TT_EOL) {
            st.getNumber();
            points.add((int) st.nval - 1);
//			st.getNumber();
            st.getToken();
            while (st.ttype == '/') {
                // 忽略'/'后面的数据，只读取顶点数据
                st.getNumber();
                st.getToken();
            }
            if(st.ttype==StreamTokenizer.TT_EOL)break;
            else st.pushBack();
        }

        assert (points.size() == 3);

        Face face = new Face(points.get(0), points.get(1), points.get(2));
        faceList.add(face);
        int faceIndex = faceList.size() - 1;

        // 读取面的同时，完善相关顶点的属性
        for (int i = 0; i < points.size(); i++) {
            Vertex vertex1 = vertexList.get(points.get(i));
            vertex1.adjacentFacesIndex.add(faceIndex);
//            vertex1.normal = face.normal;

            for (int j = i + 1; j < points.size(); j++) {
                Vertex vertex2 = vertexList.get(points.get(j));
                vertex1.adjacentVerticesIndex.add(points.get(j));
                vertex2.adjacentVerticesIndex.add(points.get(i));
            }
        }
        st.skipToNextLine();

//        if (BuildConfig.DEBUG && LoggerConfig.ANDROID_DEBUG) {
//            Log.d(TAG, "readFace: " + face);
//        }
    } // End of readFace

    /**
     * 设置基本路径
     * @param pathName 路径名
     */
    private void setBasePath(String pathName) {
        basePath = pathName;
        if (basePath == null || basePath == "")
            // 使用当前路径
            basePath = "." + java.io.File.separator;
        basePath = basePath.replace('/', java.io.File.separatorChar);
        basePath = basePath.replace('\\', java.io.File.separatorChar);
        if (!basePath.endsWith(java.io.File.separator))
            basePath = basePath + java.io.File.separator;
    }

    /**
     * 设置包含文件名的文件路径
     *d @param fileName 文件名
     */
    private void setBasePathFromFilename(String fileName) {
        if (fileName.lastIndexOf(java.io.File.separator) == -1) {
            // No path given - current directory
            setBasePath("." + java.io.File.separator);
        } else {
            setBasePath(fileName.substring(0, fileName.lastIndexOf(java.io.File.separator)));
        }
    }

    private void computeAllCost() {
        for (Face f : faceList) {
            f.computeK();
        }
        for (Vertex v : vertexList) {
            v.computeQ();
        }
        for (int i = 0; i < vertexList.size(); i++) {
            Vertex v = vertexList.get(i);
            v.computeCostAndCandidate();
            costHeap.insert(i, v.cost);
        }
    }

    private void collapse(int vIndex) {
        // 待收缩的两个点
        Vertex v0 = vertexList.get(vIndex);
        Vertex v1 = vertexList.get(v0.candidateIndex);

        // 待收缩的两个点的索引
        final int v0Index = vIndex;
        final int v1Index = v0.candidateIndex;

//        // 如果是孤立的点，则直接删除
//        if (v0.cost == 0) {
//            vertexList.set(v0Index, null);
//            costHeap.delete(v0Index);
//            return;
//        }

        // 获取v0,v1相邻的面列表，并删除共有的面
        Set<Integer> fIndices = new HashSet<>();
        fIndices.addAll(v0.adjacentFacesIndex);
        fIndices.addAll(v1.adjacentFacesIndex);
        for (int vfIndex : v0.adjacentFacesIndex) {
            Face f = faceList.get(vfIndex);
            if (f.hasVertex(v1Index)) {
                // 删除共有的面，不使用remove() 方法是因为不能改变列表的索引
                faceList.set(vfIndex, null);
                fIndices.remove(vfIndex);

                // 更新另外一个顶点的面索引
                for (int i : f.verticesIndex) {
                    if (i != v0Index && i != v1Index) {
                        Vertex v = vertexList.get(i);
                        v.adjacentFacesIndex.remove(vfIndex);
                    }
                }
            }
        }

        // 获取v0,v1相邻的顶点列表，不包括v0和v1
        Set<Integer> vIndices = new HashSet<>();
        vIndices.addAll(v0.adjacentVerticesIndex);
        vIndices.addAll(v1.adjacentVerticesIndex);
        vIndices.remove(v0Index);
        vIndices.remove(v1Index);

        // 获取新顶点，为了缩减队列的长度，将新顶点放在原顶点v0的位置，删除v1（置为null）
        Vertex newVertex = new Vertex(v0.bestPosition);
        vertexList.set(v0Index, newVertex);
        vertexList.set(v1Index, null);
        // 如果需要采用预测-校正的方法，可以将下面两行注释掉
        newVertex.Q = new float[16];
        MatrixHelper.add(newVertex.Q, v0.Q, v1.Q);

        // 更新相邻的面
        for (int i : fIndices) {
            Face f = faceList.get(i);
            f.replaceVertex(v1Index, v0Index);     // 只有v1相邻的面需要更新顶点位置
            f.update();                           // 所有的面都需要更新法向量和基础二次方误差矩阵
            newVertex.adjacentFacesIndex.add(i);
        }

        // 更新相邻的顶点
        for (int i : vIndices) {
            Vertex v = vertexList.get(i);
            v.adjacentVerticesIndex.remove(v1Index);
            v.adjacentVerticesIndex.add(v0Index);
            newVertex.adjacentVerticesIndex.add(i);
        }

        // 重新计算相关顶点的法向量、二次误差矩阵和消耗
        // 如果需要采用预测-校正的方法，请取消下面注释
//        for (int i : vIndices) {
////            vertexList.get(i).computeNormal();  // 暂时不需要用到法向量
//            vertexList.get(i).computeQ();
//        }
////        newVertex.computeNormal();
//        newVertex.computeQ();
        
        for (int i : vIndices) {
            Vertex v = vertexList.get(i);
            v.computeCostAndCandidate();
            costHeap.change(i, v.cost);
        }
        newVertex.computeCostAndCandidate();
        costHeap.insert(v0Index, newVertex.cost);
        costHeap.delete(v1Index);

        // 顶点数量减1
        vN = vN - 1;
        // 三角面数量减2
        fN = fN - 2;
    }


    public class Vertex {

        public Vector position;
//        public Vector normal;

        public float[] Q;         // error quardic
        public int candidateIndex;            // 收缩的另一个端点的索引
        public float[] bestPosition;        // 代价最小的收缩顶点的位置
        public float cost;              // 最小收缩代价

        // 为了减少程序的复杂性，加快简化速度，不允许相邻点和相邻面的列表中出现重复或者无效的元素索引，牺牲空间为代价
        public Set<Integer> adjacentVerticesIndex = new HashSet<>();         // 相邻顶点的索引
        public Set<Integer> adjacentFacesIndex = new HashSet<>();            // 相邻面的索引

        public Vertex() {
            position = new Vector();
        }

        public Vertex(float[] a) {
            position = new Vector(a);
        }

//        public Vertex(float x, float y, float z) {
//            position = new Vector();
//            position.x = x;
//            position.y = y;
//            position.z = z;
//        }
//
//        public Vertex(Vector v) {
//            if (v == null) {
//                throw new NullPointerException();
//            }
//            position = v;
//        }

        /**
         * 使用平均向量法计算顶点的法向量
         */
//        public void computeNormal() {
//            float[] sum = new float[]{0, 0, 0};
//            for (int fIndex : adjacentFacesIndex) {
//                Face f = faceList.get(fIndex);
//                sum[0] += f.normal.x;
//                sum[1] += f.normal.y;
//                sum[2] += f.normal.z;
//            }
//            sum[0] /= adjacentFacesIndex.size();
//            sum[1] /= adjacentFacesIndex.size();
//            sum[2] /= adjacentFacesIndex.size();
//            normal = new Vector(sum);
//        }

        /**
         * 计算每个顶点的二次方误差矩阵，必须先调用Face 的computeK() 方法计算面的基础二次方误差矩阵
         */
        public void computeQ() {
            // Q = sum(K)
            Q = new float[16];
            for (int fIndex : adjacentFacesIndex) {
            	Face f = faceList.get(fIndex);
                float[] temp = new float[16];
                if ((mode & MODE_QEM_A) == MODE_QEM_A) {
                	MatrixHelper.multiplyK(temp, f.K, f.area);
                } else if ((mode & MODE_QEM_V) == MODE_QEM_V) {
                	MatrixHelper.multiplyK(temp, f.K, f.area * f.area);
                } else {
                	temp = f.K;
                }
                
                MatrixHelper.add(Q, Q, temp);
            }
        }

        /**
         * 计算该点最小的折叠代价以及最佳的折叠位置
         */
        public void computeCostAndCandidate() {
            cost = Float.MAX_VALUE;
            if (adjacentVerticesIndex.size() == 0) {
                // 如果该点为孤立的点，则优先收缩
                cost = 0;
            }
            for (int vIndex : adjacentVerticesIndex) {
                float[] tempPosition = new float[4];
                float tempCost = computeCostCollapseTo(vIndex, tempPosition);
                if (tempCost < cost) {
                    candidateIndex = vIndex;
                    cost = tempCost;
                    bestPosition = tempPosition;
                }
            }
        }

        private float computeCostCollapseTo(int vIndex, float[] newPosition) {
            Vertex v = vertexList.get(vIndex);

            // Qe = Q1 + Q2
            float[] Qe = new float[16];
            MatrixHelper.add(Qe, this.Q, v.Q);

            float[] t = Arrays.copyOf(Qe, Qe.length);
            t[3] = t[7] = t[11] = 0;
            t[15] = 1;
            float[] Qe_v = new float[16];
            float cost = Float.MAX_VALUE;
            if (MatrixHelper.invertM(Qe_v, t)) {
                // 计算新顶点的位置
                MatrixHelper.multiplyMV(newPosition, Qe_v, new float[]{0, 0, 0, 1});

                float[] temp = new float[4];
                MatrixHelper.multiplyMV(temp, Qe, newPosition);
                cost = MatrixHelper.dotProduct(newPosition, temp);
            } else{
                // 矩阵不可逆，则选择收缩点在两个端点或者中点
                // 收缩点选在此端点
                float[] v1 = this.position.toFloatArray();
                float[] temp = new float[4];
                MatrixHelper.multiplyMV(temp, Qe, v1);
                float cost1 = MatrixHelper.dotProduct(v1, temp);
                if (cost1 < cost) {
                    cost = cost1;
                    System.arraycopy(v1, 0, newPosition, 0, 4);
                }

                // 收缩点为另一端点
                float[] v2 = v.position.toFloatArray();
                MatrixHelper.multiplyMV(temp, Qe, v2);
                float cost2 = MatrixHelper.dotProduct(v2, temp);
                if (cost2 < cost) {
                    cost = cost2;
                    System.arraycopy(v2, 0, newPosition, 0, 4);
                }

                // 收缩点为中点
                float[] v3 = new float[]{
                        (this.position.x + v.position.x) / 2,
                        (this.position.y + v.position.y) / 2,
                        (this.position.z + v.position.z) / 2,
                        1};
                MatrixHelper.multiplyMV(temp, Qe, v3);
                float cost3 = MatrixHelper.dotProduct(v3, temp);
                if (cost3 < cost) {
                    cost = cost3;
                    System.arraycopy(v3, 0, newPosition, 0, 4);
                }
            }

            if ((mode & MODE_QEM_N) == MODE_QEM_N) {
                List<Integer> sides = new ArrayList<>();
                for (int fIndex : this.adjacentFacesIndex) {
                    if (faceList.get(fIndex).hasVertex(vIndex)) {
                        sides.add(fIndex);
                    }
                }
                if (sides.size() == 0) {
//                    cost = 0;
                    return cost;
                }

                // 保存法向量相差最大的两个面的索引
                int mIndex = -1;
                int maxIndex = -1;

                float S = 0;    // 总面积
                float smax = 0; // 面积最大的一个面的面积

                float maxcurv = -1;
                for (int i : this.adjacentFacesIndex) {
                    Face f1 = faceList.get(i);
                    S += f1.area;
                    if (f1.area > smax) {
                        smax = f1.area;
                    }

                    float mcurv = 2;
                    for (int j : sides) {
                        Face f2 = faceList.get(j);
                        float dotProduct = f1.normal.dotProduct(f2.normal);
                        float curv = (1 - dotProduct) / 2;
                        if (curv < mcurv) {
                            mcurv = curv;
                            mIndex = j;
                        }
                    }
                    if (mcurv > maxcurv) {
                        maxcurv = mcurv;
                        maxIndex = i;
                    }
                }
                for (int i : v.adjacentFacesIndex) {
                    Face f1 = faceList.get(i);
                    S += f1.area;
                    if (f1.area > smax) {
                        smax = f1.area;
                    }

                    float mcurv = 2;
                    for (int j : sides) {
                        Face f2 = faceList.get(j);
                        float dotProduct = f1.normal.dotProduct(f2.normal);
                        float curv = (1 - dotProduct) / 2;
                        if (curv < mcurv) {
                            mcurv = curv;
                            mIndex = j;
                        }
                    }
                    if (mcurv > maxcurv) {
                        maxcurv = mcurv;
                        maxIndex = i;
                    }
                }

                // 计算影响因子
                for (int i : sides) {
                    S -= faceList.get(i).area;
                }

//                float alpha = (faceList.get(minIndex).area + faceList.get(maxIndex).area) / S;
                float alpha = 4 * (faceList.get(mIndex).area + faceList.get(maxIndex).area) / S;
//                float alpha = (faceList.get(minIndex).area + faceList.get(maxIndex).area) / (2 * smax);
                if (alpha > 1) {
                    alpha = 1;
                }
//                System.out.println("Alpha:" + alpha);

                cost = cost * (1 - alpha + alpha * maxcurv);
            }

            return cost;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Position:" + position);
//            stringBuilder.append("Normal:" + normal + "\n");
//            stringBuilder.append("Neighbors: ");
//            for (int vIndex : adjacentVerticesIndex) {
//                stringBuilder.append(vIndex + " ");
//            }
//            stringBuilder.append("\nFaces: ");
//            for (int fIndex : adjacentFacesIndex) {
//                stringBuilder.append(fIndex + " ");
//            }
            return stringBuilder.toString();
        }
    }

    private class Face {

        public final int[] verticesIndex = new int[3];
        public Vector normal;      // 面的单位法向量
        public float[] K;          // fundamental error quadric

        private float area;        // 面积，仅在MODE_QEM_A 模式下有效


        public Face(int vIndex1, int vIndex2, int vIndex3) {
            verticesIndex[0] = vIndex1;
            verticesIndex[1] = vIndex2;
            verticesIndex[2] = vIndex3;

            computeNormalAndArea();
        }

        public boolean hasVertex(int vIndex) {
            for (int i : verticesIndex) {
                if (i == vIndex) {
                    return true;
                }
            }
            return false;
        }

        public boolean replaceVertex(int oldIndex, int newIndex) {
            for (int i = 0; i < 3; i++) {
                if (verticesIndex[i] == oldIndex) {
                    verticesIndex[i] = newIndex;
                    return true;
                }
            }
            return false;
        }

        /**
         * 更新法向量、面积及二次方误差矩阵，当面的顶点改变时需调用此方法
         */
        public void update() {
            computeNormalAndArea();
//            computeK();
        }

        /**
         * 计算每个面的基础二次方误差矩阵
         */
        public void computeK() {
            // suppose the plane(face) is denoted as p = [a, b, c, d]^T
            Vertex oneVertex = vertexList.get(verticesIndex[0]);            // 任取面的一个顶点
            float d = - normal.dotProduct(oneVertex.position);
            float[] p = new float[]{normal.x, normal.y, normal.z, d};

            // K = p * p^T
            K = new float[16];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    // 注意，这里为了与OpenGL 兼容，使用列向量的方式组织矩阵
                    K[i + j * 4] = p[i] * p[j];
                }
            }
        }

        private void computeNormalAndArea() {
            Vertex p1 = vertexList.get(verticesIndex[0]);
            Vertex p2 = vertexList.get(verticesIndex[1]);
            Vertex p3 = vertexList.get(verticesIndex[2]);
            Vector n = Vector.minus(p1.position, p2.position).crossProduct(Vector.minus
                    (p3.position, p2.position));
            normal = n.normalize();
            area = n.magnitude() / 2;
        }

        @Override
        public String toString() {
            return "(" + verticesIndex[0] + "," + verticesIndex[1] + "," + verticesIndex[2] + ")";
        }
    }

//    private class CollapseRecord {
//        public int vIndex1;
//        public Vector p1;
//        public int vIndex2;
//        public Vector p2;
//        public int newIndex;
//        public Vector p;
//
//        public void setOriginalVertex(int vIndex) {
//            vIndex1 = vIndex;
//            p1 = vertexList.get(vIndex1).position;
//            vIndex2 = vertexList.get(vIndex1).candidateIndex;
//            p2 = vertexList.get(vIndex2).position;
//        }
//
//        public void setNewVertex(int vIndex) {
//            newIndex = vIndex;
//            p = vertexList.get(newIndex).position;
//        }
//    }
}
