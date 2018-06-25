/*******************************************************************************
 * Copyright (c) 2017 Pablo Pavon Marino and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 2-clause BSD License 
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/BSD-2-Clause
 *
 * Contributors:
 *     Pablo Pavon Marino and others - initial API and implementation
 *******************************************************************************/

package com.net2plan.research.metrohaul.networkModel;

import com.net2plan.interfaces.networkDesign.NetworkLayer;

public class WLayerWdm extends WAbstractNetworkElement
{
	final private NetworkLayer wdmLayer;

	WLayerWdm (NetworkLayer l) { super (l); this.wdmLayer = l; }

	@Override
	public NetworkLayer getNe() { return wdmLayer; }
	
    
	
}
