package RandamPackage;
import Agent.Agent_Rwaypoint;

public class RandomWay_Rwaypoint {
	public static int RandamPickNearAgent(int p, int n, Agent_Rwaypoint agent[], int DI){
		for(int i=0; i<n; i++){
			if(p!=i){
				if((agent[p].getx() - agent[i].getx())*(agent[p].getx() - agent[i].getx())
						+(agent[p].gety() - agent[i].gety())*(agent[p].gety() - agent[i].gety())  <= DI)
				return i;
			}
		}
		return -1;
	}
}
