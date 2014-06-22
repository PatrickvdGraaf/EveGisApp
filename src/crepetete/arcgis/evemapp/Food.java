package crepetete.arcgis.evemapp;

public class Food {
	
	//Food is een class die ervoor zorgt dat een menu van een FoodStand overzichtelijk opgebouwd kan worden. 
	//De foodstand heeft een lijst met Food, en elk voedsel heeft uiteraard een naam en een prijs. Deze worden hier gedefinieerd.
	
	private String name;
	private Double price;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}
