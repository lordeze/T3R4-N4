package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.S_Inven;

/**
 * Клиентский пакет, запрасывающий пакет инвенторя.
 *
 * @author Ronn Mod Evestus
 */
public class RequestInventoryInfo extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();
                //System.out.println("Client Packet: "+owner.getOwner());
	}

	@Override
	public void runImpl()
	{
		if(player != null) {
                        //System.out.println("S_Inven: "+S_Inven.getInstance(player));
			player.sendPacket(S_Inven.getInstance(player), true);
                }
	}
}