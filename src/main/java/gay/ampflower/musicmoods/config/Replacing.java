/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.config;// Created 2023-03-02T01:09:52

/**
 * @author Ampflower
 * @since ${version}
 **/
public enum Replacing {
	never,
	allow,
	always,;

	public boolean replaces() {
		return this != never;
	}
}
