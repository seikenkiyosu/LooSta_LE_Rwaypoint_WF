import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;

import Agent.Agent_Rwaypoint;
import Interaction.Interaction_Rwaypoint;
import RandamPackage.*;

class Loosta_LE_RWP_WF{
	public static final int Gridsize = 50;
	public static final long Roundnum = 1000000000000L;
	public static final int DataNum = 100;	//データ数
	
	public static final int s_from = 11;			//sはs_from=s_toのデータをとる
	public static final int s_to = 12;
	public static final int sinterval = 1;
	
	public static final int ninterval = 10;
	public static final int n_from = 10;	//nはn_from～n_toのデータをとる
	public static final int n_to = 50;	
	
	public static String RandomMethod = "Rway-point";
	public static String name = "星顕";
//	System.getProperty("user.name");
	
	public static String DataPath = "\\Users\\" + name + "\\Dropbox\\Data\\";
	public static String WritingPath = DataPath + "Data_" + RandomMethod + "\\";
	
	public static final int r = 1;	//vの速度の上限
	public static final int DistanceforInteraction = 1;	//interactionができる距離
	
	public static void main(String args[]){
		Random random = new Random();
		File file = new File(WritingPath);
        if (!file.exists()) {
            System.out.println("ディレクトリが存在しません。");
            System.exit(-1);
        }
        
        for(int s=s_from; s<=s_to; s+=sinterval){
        	for(int n=n_from; n<=n_to; n+=ninterval){
				Agent_Rwaypoint agent[] = new Agent_Rwaypoint[n];
				long CTsum=0, HTsum=0;
				double CTave=0.0 , HTave=0.0;
				for(int Data=0; Data < DataNum; Data++){
					long CT = 0, HT = 0;
					boolean HT_count_flag = false, CT_count_flag = true;
					double R[] = new double[n];			//RWPによるAgent[i]の行く先を格納(Destination)
					double THETA[] = new double[n];		
					double MoveDis[] = new double[n];	//Destinationが決まってからどれだけ進んでるか
					boolean HaveDest[] = new boolean[n];	//Agent[i]がdestinationを持ってるか
					
					/*Agent Initialization*/
					for(int i=0; i<n; i++){
						agent[i] = new Agent_Rwaypoint(random.nextBoolean(), random.nextInt(Gridsize)+random.nextDouble(), random.nextInt(Gridsize)+random.nextDouble(), s);
						HaveDest[i] = false;
					}
						
					for(int i=0; i<Roundnum; i++){
						int leadercount=0;
						//リーダの数をかぞえる
						for(int j=0; j<n; j++) if(agent[j].IsLeader()){ leadercount++; }
						//Holding Timeが終了したらぬける
						if(leadercount!=1 && HT_count_flag==true){ break; }
		//				System.out.println("the number of leaders = " + leadercount);
						//リーダが決まったとき
						if(IsSafeConfiguration(s, n, leadercount, agent)==true){ 
							HT_count_flag = true;
							CT_count_flag = false;
						}
						//リーダが一個のとき
						if(HT_count_flag==true) HT++;
						if(CT_count_flag==true) CT++;
						
						while(true){					//一回の交流がちゃんと終わるまで				
							for(int j=0; j<n; j++){			//for each node
								/*decide destination begin*/
								if(HaveDest[j] == false){
									R[j] = random.nextInt(Gridsize)+random.nextDouble();
									THETA[j] = random.nextInt(360)+random.nextDouble();
									MoveDis[j] = 0.0;
									while(0>agent[j].getx()+R[j]*Math.cos(THETA[j])||agent[j].getx()+R[j]*Math.cos(THETA[j])>Gridsize
											||0>agent[j].gety()+R[j]*Math.sin(THETA[j])||agent[j].gety()+R[j]*Math.sin(THETA[j])>Gridsize){
										R[j] = random.nextInt(Gridsize)+random.nextDouble();
										THETA[j] = random.nextInt(360)+random.nextDouble();
									}
									HaveDest[j] = true;
								}
								/*decide destination end*/
								/*agent move process begin*/
								if(r+MoveDis[j] < R[j]){
									double vr = r;	//ランダムに次のラウンドで動く距離を決める
									if(MoveDis[j]+vr >= R[j]) {	//超えそうになったとき制御
										vr = R[j]-MoveDis[j];
										HaveDest[j] = false;
									}
									agent[j].ShiftPointForRWP( vr, THETA[j]);	//移動
									MoveDis[j] += vr;
								}
								else {
									double vr = R[j]-MoveDis[j];
									HaveDest[j] = false;
									agent[j].ShiftPointForRWP( vr, THETA[j]);
									MoveDis[j] += vr;
								}
								/*agent move process end*/
							}
							/*interaction process begin*/
							int p = random.nextInt(n);		//interactionをするagentをランダムで選択
							int q = RandomWay_Rwaypoint.RandamPickNearAgent( p, n, agent, DistanceforInteraction);		//pからDI離れた範囲でランダムにAgentを拾ってqに代入
							if(q != -1) { 	//qが見つかったら
								Interaction_Rwaypoint.interaction(agent[p], agent[q], s);		//pとqをinteractionさせる
	//							for(int j=0; j<n; j++) agent[j].Countdown();	//interactionしたagentのtimerをデクリメント
								break;
							}//次のラウンドへ
							/*interaction process end*/
						}
					}
				    CTsum += CT;
				    if(CT+HT == Roundnum) { HT = Roundnum; }
				    HTsum += HT;
					System.out.println("( " + RandomMethod + " " + " " + r + "R_" + DistanceforInteraction + "DI_" + Gridsize + "GS_" + DataNum + "DN"
							+ "  s:" + s +  ", n:" + n_from + "~" + n_to + " )"
							+ "\t n = " + n
							+ ", Data number = " + (Data+1)
							+ ",\tCT = " + CT + ",\tHT = " + HT);
				}	//Dataのfor文終了
				CTave = (double)CTsum / DataNum;
				HTave = (double)HTsum / DataNum;
				/*ファイル書き込みのための処理*/
				try{
			        String stringCTave = String.valueOf(CTave);
			        String stringHTave;
			        if(HTave != 0){
			        	stringHTave = String.valueOf(HTave);
			        }
			        else { stringHTave = "Not Have a safe configuration";}
			        
					String nvalue = new Integer(n).toString();
					String sfromvalue = new Integer(s_from).toString();
					String stovalue = new Integer(s_to).toString();
	
			        File file_nCT = new File(WritingPath + "CT\\" + "n=" + nvalue + "_s=from" + sfromvalue + "to" + stovalue + "_EP" + ".txt");
			        File file_nHT = new File(WritingPath + "HT\\" + "n=" + nvalue + "_s=from" + sfromvalue + "to" + stovalue + "_EP" + ".txt");
	
			        if(!file_nCT.exists()){ file_nCT.createNewFile(); }
			        if(!file_nHT.exists()){ file_nHT.createNewFile(); }
	
			        PrintWriter pw_nCT = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file_nCT, true)));
			        PrintWriter pw_nHT = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file_nHT, true)));
	
			        pw_nCT.write(stringCTave + "\r\n");
			        pw_nHT.write(stringHTave + "\r\n");
	
			        pw_nCT.close();
			        pw_nHT.close();
			        
					String svalue = new Integer(s).toString();
					String nfromvalue = new Integer(n_from).toString();
					String ntovalue = new Integer(n_to).toString();
	
			        File file_sCT = new File(WritingPath + "CT\\" + "s=" + svalue + "_n=from" + nfromvalue + "to" + ntovalue + "_EP" + ".txt");
			        File file_sHT = new File(WritingPath + "HT\\" + "s=" + svalue + "_n=from" + nfromvalue + "to" + ntovalue + "_EP" + ".txt");
	
			        if(!file_sCT.exists()){ file_sCT.createNewFile(); }
			        if(!file_sHT.exists()){ file_sHT.createNewFile(); }
	
			        PrintWriter pw_sCT = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file_sCT, true)));
			        PrintWriter pw_sHT = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file_sHT, true)));
	
			        pw_sCT.write(stringCTave + "\r\n");
			        pw_sHT.write(stringHTave + "\r\n");
	
			        pw_sCT.close();
			        pw_sHT.close();
	
			      }catch(IOException e){
			        System.out.println(e);
			      }
			}	//end  for(int n=n_from; n<=n_to; n++)
        }	//end for(int s=s_from; s<=s_to; s++)
	}
	/*安全状態であるかを返す*/
	private static boolean IsSafeConfiguration(int timerupper, int agentnum, int leadercount, Agent_Rwaypoint agent[]){
		boolean issafe = true;
		if(leadercount==1){
			for(int i=0; i < agentnum; i++){
				if(agent[i].gettimer() < timerupper/2){
					issafe = false;
					break;
				}
			}
			if(issafe == true){ return true; }	//leaderが一個かつ全個体のタイマがs/2以上のとき安全状態
		}
		return false;
	}
}
