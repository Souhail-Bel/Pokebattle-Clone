public class Pokemon{
    //this is suffient for a pokemon
    String name;
    int attack;
    int defense;
    public Pokemon(String name){
	//TODO add fetchin from a db
    }
    public Pokemon( String name, int attack, int defense){
	this.name=name;
	this.attack=attack;
	this.defense=defense;
    } 
}
